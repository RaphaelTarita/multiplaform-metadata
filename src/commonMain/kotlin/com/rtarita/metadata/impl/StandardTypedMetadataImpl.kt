package com.rtarita.metadata.impl

import com.rtarita.metadata.TypedMetadata
import com.rtarita.metadata.delegate.MetadataDelegate
import com.rtarita.metadata.delegate.TypedMetadataDelegateImpl
import com.rtarita.metadata.filterNonNullValues
import kotlin.reflect.KClass

internal class StandardTypedMetadataImpl<T>(
    override val name: String,
    override val type: KClass<*>,
    internal val notifyCreate: (identifier: String, submap: StandardTypedMetadataImpl<T>) -> Unit,
    internal val notifyRemove: (identifier: String) -> Unit,
    initSize: Int = 0,
    loadFactor: Float = 0.75f
) : TypedMetadata<T> {
    internal val map: MutableMap<String, T?> = HashMap(initSize, loadFactor)

    override fun getOrNull(identifier: String): T? {
        return map[identifier]
    }

    internal fun setSilent(identifier: String, value: T) {
        map[identifier] = value
    }

    override operator fun set(identifier: String, value: T) {
        notifyCreate(identifier, this)
        map[identifier] = value
    }

    internal fun removeSilent(identifier: String) {
        if (map[identifier] != null) {
            map.remove(identifier)
        }
    }

    override fun remove(identifier: String): T? {
        return if (map[identifier] != null) {
            notifyRemove(identifier)
            map.remove(identifier)
        } else {
            null
        }
    }

    override fun exists(identifier: String): Boolean {
        return map[identifier] != null
    }

    override fun bind(identifier: String): MetadataDelegate<T> = TypedMetadataDelegateImpl(this, identifier)

    override fun allEntries(): Map<String, T> {
        return map.filterNonNullValues()
    }


    override val nullable: StandardNullableTypedMetadataImpl<T> = StandardNullableTypedMetadataImpl(this)
    override val isNullable: Boolean = false
    override val reservedPrefixes: Set<String> = setOf("__")

    override fun toString(): String {
        return "$name: ${allEntries()}"
    }
}
