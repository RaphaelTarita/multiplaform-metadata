package com.rtarita.metadata

import com.rtarita.metadata.delegate.MetadataDelegate
import com.rtarita.metadata.exc.UnknownIdentifierException
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

public interface TypedMetadata<T> {
    public operator fun get(identifier: String): T {
        return getOrNull(identifier) ?: if (type == Any::class) throw UnknownIdentifierException(identifier)
        else throw UnknownIdentifierException(identifier, type, isNullable)
    }

    public fun getOrNull(identifier: String): T?
    public operator fun set(identifier: String, value: T)
    public fun remove(identifier: String): T?
    public fun exists(identifier: String): Boolean
    public fun existsNot(identifier: String): Boolean = !exists(identifier)
    public fun bind(identifier: String): MetadataDelegate<T>
    public operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): MetadataDelegate<T> = bind(property.name)
    public fun allEntries(): Map<String, T>
    public val nullable: NullableTypedMetadata<T>

    public val name: String
    public val type: KClass<*>
    public val isNullable: Boolean
    public val reservedPrefixes: Set<String>
}