package com.rtarita.metadata.delegate

import com.rtarita.metadata.TypedMetadata
import kotlin.reflect.KProperty

internal class TypedMetadataDelegateImpl<T>(
    private val metadata: TypedMetadata<T>,
    private val identifier: String
) : MetadataDelegate<T> {
    override operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return metadata[identifier]
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        metadata[identifier] = value
    }
}