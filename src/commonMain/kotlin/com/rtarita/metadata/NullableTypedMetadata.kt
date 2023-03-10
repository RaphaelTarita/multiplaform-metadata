package com.rtarita.metadata

public interface NullableTypedMetadata<T> : TypedMetadata<T?> {
    override val nullable: NullableTypedMetadata<T?>
        @Suppress("UNCHECKED_CAST") get() = this as NullableTypedMetadata<T?>
    override val isNullable: Boolean
        get() = true
}