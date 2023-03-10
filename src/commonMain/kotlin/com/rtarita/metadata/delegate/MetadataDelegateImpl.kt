package com.rtarita.metadata.delegate

import com.rtarita.metadata.Metadata
import com.rtarita.metadata.TypedMetadata
import com.rtarita.metadata.exc.UnknownIdentifierException
import kotlin.reflect.KProperty

internal class MetadataDelegateImpl(
    private val metadata: Metadata,
    private val identifier: String
) : MetadataDelegate<Any> {
    private var submapCache: TypedMetadata<*> = refreshCache()

    private fun refreshCache(): TypedMetadata<*> {
        return metadata.locate(identifier) ?: metadata
    }

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Any {
        return try {
            submapCache[identifier]!!
        } catch (_: UnknownIdentifierException) {
            refreshCache()
            metadata[identifier]
        }
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Any) {
        metadata[identifier] = value
    }
}