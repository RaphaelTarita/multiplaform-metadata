# multiplatform-metadata

This repository contains an API and a sample implementation for an in-memory, null-safe and optionally typed metadata
storage, implemented entirely in common Kotlin code. It can be used on JVM, JS, Native and all other existing or future
platforms Kotlin can compile to.

### What exactly is this?

At its core, it's just a simple key-value storage. Keys are strings, and values can be virtually any Kotlin object. A
key can be associated with an object of a certain type, and later queried or re-assigned (also to a different type). The
specialty of this API is that it supports optionally typed access to fit better into Kotlin, which is inherently a
statically typed language. The metadata store is also null-safe, meaning that the API deals explicitly with nullability.

### What is the purpose?

The idea behind this project is to have a general-purpose "context object" that can be passed around as parameter, as
context receiver or stored globally. Parts of the program can write values into it, and other parts can access them.

I originally came up with the idea when I was working on an image processing library. For a certain transformation it
was necessary to gather information in one step and then pass it downstream to the next step, which would use it. In
order to not have to break the call chain or assign variables outside the chain, a metadata object could be passed
through the entire stream (pseudocode):

```kotlin
// without a metadata object
var greyVal = 0
var xCoord = 0

img.map { pixel ->
    // ...
    greyVal = pixel.greyscale
    xCoord = pixel.x
}.map { pixel ->
    if (pixel.greyscale > greyVal || pixel.x < xCoord) {
        // ...
    }
}
```

```kotlin
// with metadata
img.mapWithMetadata { pixel, metadata ->
    // ...
    metadata["greyVal"] = pixel.greyscale
    metadata["xCoord"] = pixel.x
}.mapWithMetadata { pixel, metadata ->
    if (pixel.greyscale > metadata.doubles["greyVal"] || pixel.y < metadata.ints["xCoord"]) {
        // ...
    }
}
```

### How does it work in practice?

There are multiple ways to interact with the `Metadata` API, and the following code samples shall demonstrate some of
them. Note that the consolidated code sample is also available in runnable form under `src/jvmMain/kotlin/test.kt`.

#### Initialization

```kotlin
val metadata: Metadata = StandardMetadataImpl("test")
```

Every `Metadata` object needs to be initialized with some implementation of the interface. In this
repository, `StandardMetadataImpl` is available as a simple sample implementation. This implementation also stores a
name for every metadata object.

#### Simple Untyped Interaction

```kotlin
metadata["intKey1"] = 42
metadata["intKey2"] = 8
metadata["stringKey"] = "this is a string"

println(metadata["intKey1"]) // 42
println(metadata["intKey2"]) // 8
println(metadata["stringKey"]) // this is a string
```

Using the indexed access operator (`operator fun get/set`), values can be assigned or retrieved without worrying about
the type. If we try to read a value that does not exist, `UnknownIdentifierException` will be thrown.

When retrieving a value in this way, its type will be `Any`. However, only working with `Any` values is likely
to become very tedious due to Kotlin's statically typed nature. Let's look at how we can optionally use a typed API:

#### Simple Typed Interaction

```kotlin
println(metadata.ints["intKey1"] + metadata.ints["intKey2"]) // 50
println(metadata.strings["stringKey"].uppercase()) // THIS IS A STRING
```

By suffixing the `metadata` object with the type we expect (pluralized), we can retrieve values in a typed fashion. As
demonstrated, using this method it's possible to perform type-specifc operations (addition for `Int`s, `.uppercase()`
function call for `String`s) on the values.

If the expected type does not match the actual type of the value that's associated with the key,
an `UnknownIdentifierException` will be thrown. The reason for this is that conceptually, the `ints` / `strings` / ...
properties represent _subsets_ of the entire metadata object which only contain the values of that specific type.
Therefore, if we'd try to access `metadata.ints["stringKey"]`, `UnknownIdentifierException` would be thrown because
the `ints` subset does not contain that key.

The following typed subsets are available:

- `bytes`
- `shorts`
- `ints`
- `longs`
- `floats`
- `doubles`
- `booleans`
- `chars`
- `strings`

If a type fits into neither of these, the value will be stored in a fallback map and will only be accessible through the
direct index access on the metadata object (always having the static type `Any`).

#### Mixed-Type Reassignments

```kotlin
metadata["stringKey"] = 3.14
println(metadata.doubles["stringKey"] * 2) // 6.28
```

Keys can always be re-assigned to new values, and those new values may be of a different type. In this example, we
re-assign `stringKey` (which was previously associated with a `String`) to the value `3.14` (which is a `Double`). As
we've seen previously, if we tried to access `metadata.strings["stringKey"]` _now_, an exception would be thrown because
there's no `String` associated to `stringKey`.I Instead, we access the `doubles` subset, which now contains this key. We
can also interact with the result in a statically typed fashion (note multiplication, which would not be possible
if `stringKey` was still a `String`).

#### Nullability Concept

```kotlin
metadata["nullable"] = "not yet null"
println(metadata.exists("nullable")) // true
println(metadata.nullable.exists("nullable")) // true
metadata.nullable["nullable"] = null
println(metadata.exists("nullable")) // false
println(metadata.nullable.exists("nullable")) // true
```

The same superset/subset concept for typed values also applies to nullable values. By default, any assignment or access
is non-nullable. By using the `nullable` suffix, we can access the _superset_ of all non-null values, which additionally
includes null values. As you can see, a non-null value (`"not yet null"`) is available in both `metadata`
and `metadata.nullable`. However, when `nullable` is reassigned to `null`, it is _no longer_ available in `metadata` and
only in the `nullable` superset.

Also, every typed subset (`ints`, `strings` etc.) has its own `nullable` superset.

#### Nullability In Practice

```kotlin
metadata.ints.nullable["nullableInt"] = if (Random.nextBoolean()) null else 6
metadata.ints["nonNullInt"] = 2
println((metadata.ints.nullable["nullableInt"] ?: 5) - metadata.ints["nonNullInt"]) // prints 3 or 4
```

Technically, the `nullable` superset is only ever needed if we need to access a value that is `null`, because all other
values are available in the non-nullable subset as well. However, if we cannot know for sure if a value associated to a
certain key will be null or not, we can use the `nullable` suffix to make use of Kotlin's inbuilt null-safety and
convenience operators to deal with that value. This example also demonstrates how every typed subset has a nullable
superset.

#### Order Ambiguity Between Typed And Nullable Suffixes

```kotlin
metadata.booleans.nullable["nullableBoolean"] = null
println(metadata.nullable.booleans["nullableBoolean"]) // null
```

Conceptually, every `Metadata` implements `TypedMetadata<Any>` and provides subsets for certain types as well as access
to the `nullable` superset. But `NullableMetadata` itself also has accessors for typed subsets. This means that for
every nullable typed access, there are two possible ways:

1. Access the nullable superset of the typed subset: `metadata.booleans.nullable`
2. Access the typed subset of the nullable superset: `metadata.nullable.booleans`

Both variants are valid and semantically equivalent.

#### Property Delegation

```kotlin
var delegateTest by metadata
delegateTest = 5
println(delegateTest) // 5
delegateTest = "test"
println("$delegateTest / ${metadata["delegateTest"]} / ${metadata.strings["delegateTest"]}") // test / test / test
```

The `Metadata` API contains a `provideDelegate()` operator function, which means it can be used for Kotlin's property
delegation using the `by` keyword. This function delegates the property to a value in the `metadata` object which has
the same key as the properties name (in this case, `"delegateTest"`). The delegation itself (with `by`) does not execute
any operation on the metadata store, therefore it is also safe to delegate to keys that aren't initialized yet. If you
use the delegated property for reading before the value has been initialized, `UnknownIdentifierException` will be
thrown.

Once you have delegated to a property and initialized the value, there are multiple ways to access the value:

1. Directly by using the delegated property: `delegateTest`
2. By using untyped access to the metadata store: `metadata["delegateTest"]`
3. By using typed access to the metadata store: `metadata.strings["delegateTest"]`

#### Delegates With Explicit Keys

```kotlin
var delegateWithExplicitName by metadata.doubles.bind("test-1")
delegateWithExplicitName = 5.0
println("$delegateWithExplicitName / ${metadata["test-1"]} / ${metadata.doubles["test-1"]}") // 5.0 / 5.0 / 5.0
```

Sometimes, it's not possible to name a property after the key in the metadata store, for example because it contains a
hyphen. In this case, you can explicitly bind to a name using the `bind()` function.

This example also demonstrates that delegates can be typed, and also (as previously seen) the different ways of
accessing the value afterwards.

#### Binding To Already Initialized Keys

```kotlin
metadata["someString"] = "this was initialized before it was delegated to a property"
val someString by metadata.strings
println(someString) // this was initialized before it was delegated to a property
```

The use case of property delegation is not always to assign a new value. Sometimes, you want to access an existing value
via a property. This is also trivial, you just need to name your property the same as the previously initialized key (or
use `bind()` to explicitly bind to that key string). Reading access to the delegated property is possible right away and
will yield the previously assigned value.

Also note that it is always possible to delegate to a `val` instead of `var`.

### Is this project available somewhere as a library?

No, at least not yet. Maybe one day, if I figure out how to publish kotlin multiplatform artifacts..

Until then, you are always free to copy over the code for your own projects. 