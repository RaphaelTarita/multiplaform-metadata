package com.rtarita.metadata

public interface NullableMetadata : NullableTypedMetadata<Any> {
    public val bytes: NullableTypedMetadata<Byte>
    public val shorts: NullableTypedMetadata<Short>
    public val ints: NullableTypedMetadata<Int>
    public val longs: NullableTypedMetadata<Long>

    public val floats: NullableTypedMetadata<Float>
    public val doubles: NullableTypedMetadata<Double>

    public val booleans: NullableTypedMetadata<Boolean>

    public val chars: NullableTypedMetadata<Char>

    public val strings: NullableTypedMetadata<String>

    public fun locate(identifier: String): NullableTypedMetadata<*>?

    override val nullable: NullableTypedMetadata<Any?>
        @Suppress("UNCHECKED_CAST") get() = this as NullableTypedMetadata<Any?>

    override val isNullable: Boolean
        get() = true
}