package com.rtarita.metadata

public interface Metadata : TypedMetadata<Any> {
    public val bytes: TypedMetadata<Byte>
    public val shorts: TypedMetadata<Short>
    public val ints: TypedMetadata<Int>
    public val longs: TypedMetadata<Long>

    public val floats: TypedMetadata<Float>
    public val doubles: TypedMetadata<Double>

    public val booleans: TypedMetadata<Boolean>

    public val chars: TypedMetadata<Char>

    public val strings: TypedMetadata<String>

    public fun locate(identifier: String): TypedMetadata<*>?

    override val nullable: NullableMetadata
}