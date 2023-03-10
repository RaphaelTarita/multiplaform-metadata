import com.rtarita.metadata.Metadata
import com.rtarita.metadata.impl.StandardMetadataImpl
import kotlin.random.Random

public fun main() {
    val metadata: Metadata = StandardMetadataImpl("test")

    metadata["intKey1"] = 42
    metadata["intKey2"] = 8
    metadata["stringKey"] = "this is a string"

    println(metadata["intKey1"]) // 42
    println(metadata["intKey2"]) // 8
    println(metadata["stringKey"]) // this is a string

    println(metadata.ints["intKey1"] + metadata.ints["intKey2"]) // 50
    println(metadata.strings["stringKey"].uppercase()) // THIS IS A STRING

    metadata["stringKey"] = 3.14
    println(metadata.doubles["stringKey"] * 2) // 6.28

    metadata["nullable"] = "not yet null"
    println(metadata.exists("nullable")) // true
    println(metadata.nullable.exists("nullable")) // true
    metadata.nullable["nullable"] = null
    println(metadata.exists("nullable")) // false
    println(metadata.nullable.exists("nullable")) // true

    metadata.ints.nullable["nullableInt"] = if (Random.nextBoolean()) null else 6
    metadata.ints["nonNullInt"] = 2
    println((metadata.ints.nullable["nullableInt"] ?: 5) - metadata.ints["nonNullInt"]) // prints 3 or 4

    metadata.booleans.nullable["nullableBoolean"] = null
    println(metadata.nullable.booleans["nullableBoolean"]) // null

    var delegateTest by metadata
    delegateTest = 5
    println(delegateTest) // 5
    delegateTest = "test"
    println("$delegateTest / ${metadata["delegateTest"]} / ${metadata.strings["delegateTest"]}") // test / test / test

    var delegateWithExplicitName by metadata.doubles.bind("test-1")
    delegateWithExplicitName = 5.0
    println("$delegateWithExplicitName / ${metadata["test-1"]} / ${metadata.doubles["test-1"]}") // 5.0 / 5.0 / 5.0

    metadata["someString"] = "this was initialized before it was delegated to a property"
    val someString by metadata.strings
    println(someString) // this was initialized before it was delegated to a property
}