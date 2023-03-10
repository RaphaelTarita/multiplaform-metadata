package com.rtarita.metadata.delegate

import kotlin.reflect.KProperty

public interface MetadataDelegate<T> {
    public operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    public operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
}