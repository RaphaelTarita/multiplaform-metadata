package com.rtarita.metadata.delegate

import com.rtarita.metadata.NullableMetadata
import com.rtarita.metadata.NullableTypedMetadata
import com.rtarita.metadata.exc.UnknownIdentifierException
import kotlin.reflect.KProperty

internal class NullableMetadataDelegateImpl(
    private val metadata: NullableMetadata,
    private val identifier: String
) : MetadataDelegate<Any?> {
    private var submapCache: NullableTypedMetadata<*> = refreshCache()

    private fun refreshCache(): NullableTypedMetadata<*> {
        return metadata.locate(identifier) ?: metadata
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): Any? {
        return try {
            submapCache[identifier]
        } catch (_: UnknownIdentifierException) {
            refreshCache()
            metadata[identifier]
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Any?) {
        metadata[identifier] = value
    }
}