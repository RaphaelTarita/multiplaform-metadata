package com.rtarita.metadata

import com.rtarita.metadata.delegate.MetadataDelegate
import com.rtarita.metadata.exc.UnknownIdentifierException

@Suppress("UNCHECKED_CAST")
internal fun <K, V> Map<K, V?>.filterNonNullValues(): Map<K, V> {
    return filterValues { it != null } as Map<K, V> // .mapValues { it.value!! }
}

internal fun <K, V> combineMaps(vararg maps: Map<K, V>): Map<K, V> {
    val destSize = maps.fold(0) { prev, cur -> prev + cur.size }
    val res = HashMap<K, V>(destSize)
    for (map in maps) {
        res.putAll(map)
    }
    return res
}

@Suppress("UNCHECKED_CAST")
public inline fun <reified T : Any> Metadata.bindStatic(identifier: String): MetadataDelegate<T> {
    val submap = locate(identifier) ?: throw UnknownIdentifierException(identifier, T::class)
    if (submap.type != T::class) throw UnknownIdentifierException(identifier, T::class)
    return submap.bind(identifier) as MetadataDelegate<T>
}

@Suppress("UNCHECKED_CAST")
public inline fun <reified T : Any?> NullableMetadata.bindStatic(identifier: String): MetadataDelegate<T> {
    val submap = locate(identifier) ?: throw UnknownIdentifierException(identifier, T::class, true)
    if (submap.type != T::class) throw UnknownIdentifierException(identifier, T::class, true)
    return submap.bind(identifier) as MetadataDelegate<T>
}

public fun <T> TypedMetadata<T>.getOrElse(identifier: String, orElse: T): T = getOrNull(identifier) ?: orElse

public fun <T> TypedMetadata<T>.getOrElse(identifier: String, orElse: TypedMetadata<T>.() -> T): T {
    return getOrNull(identifier) ?: orElse()
}

public fun <T> NullableTypedMetadata<T>.getOrElse(identifier: String, orElse: T?): T? {
    return try {
        get(identifier)
    } catch (_: UnknownIdentifierException) {
        orElse
    }
}

public fun <T> NullableTypedMetadata<T>.getOrElse(identifier: String, orElse: NullableTypedMetadata<T>.() -> T?): T? {
    return try {
        get(identifier)
    } catch (_: UnknownIdentifierException) {
        orElse()
    }
}

public fun <T> TypedMetadata<T>.update(identifier: String, updater: (T) -> T): T {
    val updated = updater(get(identifier))
    set(identifier, updated)
    return updated
}

public fun <T> TypedMetadata<T>.updateIfExists(identifier: String, updater: (T) -> T): T? {
    val entry = getOrNull(identifier)
    return if (entry != null) {
        val updated = updater(entry)
        set(identifier, updated)
        updated
    } else {
        null
    }
}

public fun <T> NullableTypedMetadata<T>.update(identifier: String, updater: (T?) -> T?): T? {
    val updated = updater(get(identifier))
    set(identifier, updated)
    return updated
}

public fun <T> NullableTypedMetadata<T>.updateIfExists(identifier: String, updater: (T?) -> T?): T? {
    return try {
        val updated = updater(get(identifier))
        set(identifier, updated)
        updated
    } catch (_: UnknownIdentifierException) {
        null
    }
}

public fun <T, R> TypedMetadata<T>.use(identifier: String, usage: (T) -> R): R {
    return usage(get(identifier)).also { remove(identifier) }
}

public fun <T, R> TypedMetadata<T>.ifNonNullExists(identifier: String, action: (T) -> R): R? {
    val entry = getOrNull(identifier)
    return if (entry != null) action(entry) else null
}

public fun <T, R> TypedMetadata<T>.ifExists(identifier: String, action: (T) -> R): R? {
    return if (exists(identifier)) action(get(identifier)) else null
}

public fun <T, R> NullableTypedMetadata<T>.ifNonNull(identifier: String, action: (T) -> R): R? {
    val entry = get(identifier)
    return if (entry != null) action(entry) else null
}

public fun <T> TypedMetadata<T>.filterExistent(identifiers: Iterable<String>): Map<String, T> {
    val res = mutableMapOf<String, T>()
    for (identifier in identifiers) {
        try {
            res[identifier] = get(identifier)
        } catch (_: UnknownIdentifierException) {
            // Ignore
        }
    }
    return res
}

public fun <T> TypedMetadata<T>.filterExistent(vararg identifiers: String): Map<String, T> {
    return filterExistent(identifiers.asIterable())
}

public fun <T> TypedMetadata<T>.filterExistentValues(identifiers: Iterable<String>): Collection<T> {
    return filterExistent(identifiers).values
}