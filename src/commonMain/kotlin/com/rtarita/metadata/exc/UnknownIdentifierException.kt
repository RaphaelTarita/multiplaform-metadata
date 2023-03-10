package com.rtarita.metadata.exc

import kotlin.reflect.KClass

public class UnknownIdentifierException : NoSuchElementException {
    public constructor(identifier: String)
            : super("Unknown metadata identifier: $identifier")

    public constructor(identifier: String, type: KClass<*>, nullable: Boolean = false)
            : super("Unknown metadata identifier '$identifier' for type ${type.simpleName}${if (nullable) '?' else ""}")
}