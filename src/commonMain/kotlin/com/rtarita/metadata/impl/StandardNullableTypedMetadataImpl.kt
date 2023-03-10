package com.rtarita.metadata.impl

import com.rtarita.metadata.NullableTypedMetadata
import com.rtarita.metadata.delegate.MetadataDelegate
import com.rtarita.metadata.delegate.TypedMetadataDelegateImpl
import com.rtarita.metadata.exc.UnknownIdentifierException
import kotlin.reflect.KClass

internal class StandardNullableTypedMetadataImpl<T>(
    private val nullsafe: StandardTypedMetadataImpl<T>,
) : NullableTypedMetadata<T> {

    override val type: KClass<*> = nullsafe.type
    private val mapRef: MutableMap<String, T?> = nullsafe.map
    private val notifyCreate = nullsafe.notifyCreate
    private val notifyRemove = nullsafe.notifyRemove

    override fun getOrNull(identifier: String): T? {
        return mapRef[identifier]
    }

    override operator fun get(identifier: String): T? {
        return if (mapRef.containsKey(identifier)) {
            mapRef[identifier]
        } else {
            throw UnknownIdentifierException(identifier, type, true)
        }
    }

    internal fun setSilent(identifier: String, value: T?) {
        mapRef[identifier] = value
    }

    override operator fun set(identifier: String, value: T?) {
        notifyCreate(identifier, nullsafe)
        mapRef[identifier] = value
    }

    internal fun removeSilent(identifier: String) {
        mapRef.remove(identifier)
    }

    override fun remove(identifier: String): T? {
        notifyRemove(identifier)
        return mapRef.remove(identifier)
    }

    override fun exists(identifier: String): Boolean {
        return mapRef.containsKey(identifier)
    }

    override fun bind(identifier: String): MetadataDelegate<T?> = TypedMetadataDelegateImpl(this, identifier)

    override fun allEntries(): Map<String, T?> {
        return HashMap(mapRef)
    }

    override val reservedPrefixes: Set<String> = nullsafe.reservedPrefixes
    override val name: String = nullsafe.name + " nullable"

    override fun toString(): String {
        return "$name: ${allEntries()}"
    }
}