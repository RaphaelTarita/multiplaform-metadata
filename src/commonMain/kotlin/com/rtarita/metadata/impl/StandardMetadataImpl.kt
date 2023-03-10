package com.rtarita.metadata.impl

import com.rtarita.metadata.Metadata
import com.rtarita.metadata.NullableMetadata
import com.rtarita.metadata.TypedMetadata
import com.rtarita.metadata.combineMaps
import com.rtarita.metadata.delegate.MetadataDelegate
import com.rtarita.metadata.delegate.MetadataDelegateImpl
import com.rtarita.metadata.exc.UnknownIdentifierException
import kotlin.reflect.KClass

internal class StandardMetadataImpl(
    override val name: String,
    initSize: Int = 0,
    loadFactor: Float = 0.75f,
    private val notFoundExcProvider: (identifier: String, type: KClass<*>) -> Exception = Companion::stdExcProvider
) : Metadata {
    private companion object {
        fun stdExcProvider(identifier: String, type: KClass<*>): Exception {
            return if (type == Any::class) UnknownIdentifierException(identifier)
            else UnknownIdentifierException(identifier, type)
        }
    }

    override val bytes: StandardTypedMetadataImpl<Byte> = StandardTypedMetadataImpl(
        "$name bytes",
        Byte::class,
        this::propagateCreate,
        this::propagateRemove,
        initSize,
        loadFactor
    )

    override val shorts: StandardTypedMetadataImpl<Short> = StandardTypedMetadataImpl(
        "$name shorts",
        Short::class,
        this::propagateCreate,
        this::propagateRemove,
        initSize,
        loadFactor
    )

    override val ints: StandardTypedMetadataImpl<Int> = StandardTypedMetadataImpl(
        "$name ints",
        Int::class,
        this::propagateCreate,
        this::propagateRemove,
        initSize,
        loadFactor
    )
    override val longs: StandardTypedMetadataImpl<Long> = StandardTypedMetadataImpl(
        "$name longs",
        Long::class,
        this::propagateCreate,
        this::propagateRemove,
        initSize,
        loadFactor
    )

    override val floats: StandardTypedMetadataImpl<Float> = StandardTypedMetadataImpl(
        "$name floats",
        Float::class,
        this::propagateCreate,
        this::propagateRemove,
        initSize,
        loadFactor
    )

    override val doubles: StandardTypedMetadataImpl<Double> = StandardTypedMetadataImpl(
        "$name doubles",
        Double::class,
        this::propagateCreate,
        this::propagateRemove,
        initSize,
        loadFactor
    )

    override val booleans: StandardTypedMetadataImpl<Boolean> = StandardTypedMetadataImpl(
        "$name booleans",
        Boolean::class,
        this::propagateCreate,
        this::propagateRemove,
        initSize,
        loadFactor
    )

    override val chars: StandardTypedMetadataImpl<Char> = StandardTypedMetadataImpl(
        "$name chars",
        Char::class,
        this::propagateCreate,
        this::propagateRemove,
        initSize,
        loadFactor
    )

    override val strings: StandardTypedMetadataImpl<String> = StandardTypedMetadataImpl(
        "$name strings",
        String::class,
        this::propagateCreate,
        this::propagateRemove,
        initSize,
        loadFactor
    )

    internal val fallbackMap: StandardTypedMetadataImpl<Any> = StandardTypedMetadataImpl(
        "$name others",
        Any::class,
        this::propagateCreate,
        this::propagateRemove,
        initSize,
        loadFactor,
    )

    internal val supermap: MutableMap<String, StandardTypedMetadataImpl<*>> = HashMap(initSize * 10, loadFactor)

    override fun locate(identifier: String): TypedMetadata<*>? {
        val candidate = supermap[identifier]
        return if (candidate != null && candidate.exists(identifier)) {
            candidate
        } else {
            null
        }
    }

    private fun propagateCreate(identifier: String, submap: StandardTypedMetadataImpl<*>) {
        supermap[identifier]?.removeSilent(identifier)
        supermap[identifier] = submap
    }

    private fun propagateRemove(identifier: String) {
        supermap.remove(identifier)
    }

    override fun getOrNull(identifier: String): Any? {
        return supermap[identifier]?.get(identifier)
    }

    private fun <T> setWithSupermap(identifier: String, value: T, submap: StandardTypedMetadataImpl<T>) {
        propagateCreate(identifier, submap)
        submap.setSilent(identifier, value)
    }

    internal fun nullableSet(identifier: String, value: Any?) {
        when (value) {
            is Byte -> setWithSupermap(identifier, value, bytes)
            is Short -> setWithSupermap(identifier, value, shorts)
            is Int -> setWithSupermap(identifier, value, ints)
            is Long -> setWithSupermap(identifier, value, longs)
            is Float -> setWithSupermap(identifier, value, floats)
            is Double -> setWithSupermap(identifier, value, doubles)
            is Boolean -> setWithSupermap(identifier, value, booleans)
            is Char -> setWithSupermap(identifier, value, chars)
            is String -> setWithSupermap(identifier, value, strings)
            null -> {
                fallbackMap.nullable.setSilent(identifier, value)
                supermap[identifier] = fallbackMap
            }

            else -> setWithSupermap(identifier, value, fallbackMap)
        }
    }

    override operator fun set(identifier: String, value: Any) = nullableSet(identifier, value)

    override fun remove(identifier: String): Any? {
        return supermap[identifier]?.remove(identifier)
    }

    override fun exists(identifier: String): Boolean {
        return supermap[identifier]?.getOrNull(identifier) != null
    }

    override fun bind(identifier: String): MetadataDelegate<Any> = MetadataDelegateImpl(this, identifier)

    override fun allEntries(): Map<String, Any> {
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

    override val type: KClass<*> = Any::class
    override val isNullable: Boolean = false
    override val nullable: NullableMetadata = StandardNullableMetadataImpl(this)
    override val reservedPrefixes: Set<String> = bytes.reservedPrefixes +
            shorts.reservedPrefixes +
            ints.reservedPrefixes +
            longs.reservedPrefixes +
            floats.reservedPrefixes +
            doubles.reservedPrefixes +
            booleans.reservedPrefixes +
            chars.reservedPrefixes +
            strings.reservedPrefixes +
            fallbackMap.reservedPrefixes

    override fun toString(): String {
        return "$name: ${allEntries()}"
    }
}