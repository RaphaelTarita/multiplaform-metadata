package com.rtarita.metadata.impl

import com.rtarita.metadata.NullableMetadata
import com.rtarita.metadata.NullableTypedMetadata
import com.rtarita.metadata.combineMaps
import com.rtarita.metadata.delegate.MetadataDelegate
import com.rtarita.metadata.delegate.NullableMetadataDelegateImpl
import com.rtarita.metadata.exc.UnknownIdentifierException
import kotlin.reflect.KClass

internal class StandardNullableMetadataImpl(
    private val nullsafe: StandardMetadataImpl
) : NullableMetadata {
    override val bytes = StandardNullableTypedMetadataImpl(nullsafe.bytes)
    override val shorts = StandardNullableTypedMetadataImpl(nullsafe.shorts)
    override val ints = StandardNullableTypedMetadataImpl(nullsafe.ints)
    override val longs = StandardNullableTypedMetadataImpl(nullsafe.longs)
    override val floats = StandardNullableTypedMetadataImpl(nullsafe.floats)
    override val doubles = StandardNullableTypedMetadataImpl(nullsafe.doubles)
    override val booleans = StandardNullableTypedMetadataImpl(nullsafe.booleans)
    override val chars = StandardNullableTypedMetadataImpl(nullsafe.chars)
    override val strings = StandardNullableTypedMetadataImpl(nullsafe.strings)
    private val fallbackMap = StandardNullableTypedMetadataImpl(nullsafe.fallbackMap)

    private fun supermapAccess(identifier: String): StandardNullableTypedMetadataImpl<*>? {
        val nullsafeVersion = nullsafe.supermap[identifier]
        return when {
            nullsafeVersion === nullsafe.bytes -> bytes
            nullsafeVersion === nullsafe.shorts -> shorts
            nullsafeVersion === nullsafe.ints -> ints
            nullsafeVersion === nullsafe.longs -> longs
            nullsafeVersion === nullsafe.floats -> floats
            nullsafeVersion === nullsafe.doubles -> doubles
            nullsafeVersion === nullsafe.booleans -> booleans
            nullsafeVersion === nullsafe.chars -> chars
            nullsafeVersion === nullsafe.strings -> strings
            nullsafeVersion === nullsafe.fallbackMap -> fallbackMap
            else -> null
        }
    }

    override fun locate(identifier: String): NullableTypedMetadata<*>? {
        return supermapAccess(identifier)
    }

    override fun getOrNull(identifier: String): Any? {
        return supermapAccess(identifier)?.getOrNull(identifier)
    }

    override operator fun get(identifier: String): Any? {
        val submap = supermapAccess(identifier)
        if (submap != null) {
            return submap[identifier]
        } else {
            throw UnknownIdentifierException(identifier, Any::class, true)
        }
    }

    override fun set(identifier: String, value: Any?) {
        nullsafe.nullableSet(identifier, value)
    }

    override fun remove(identifier: String): Any? {
        return supermapAccess(identifier)?.remove(identifier)
    }

    override fun exists(identifier: String): Boolean {
        return nullsafe.supermap.containsKey(identifier)
    }

    override fun bind(identifier: String): MetadataDelegate<Any?> = NullableMetadataDelegateImpl(this, identifier)

    override fun allEntries(): Map<String, Any?> {
        return combineMaps(
            bytes.allEntries(),
            shorts.allEntries(),
            ints.allEntries(),
            longs.allEntries(),
            floats.allEntries(),
            doubles.allEntries(),
            booleans.allEntries(),
            chars.allEntries(),
            strings.allEntries(),
            fallbackMap.allEntries()
        )
    }

    override val name: String = "${nullsafe.name} nullable"
    override val type: KClass<*> = Any::class
    override val reservedPrefixes: Set<String> = nullsafe.reservedPrefixes

    override fun toString(): String {
        return "$name: ${allEntries()}"
    }
}