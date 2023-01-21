package com.github.sessionkotlin.parser.symbols.values

import com.github.sessionkotlin.parser.exception.IncompatibleTypesException

public fun String.toVal(): StringValue = StringValue(this)

/**
 * API for string values.
 */
public data class StringValue(override val value: String) : RefinedValue(value) {
    override fun unaryMinus(): RefinedValue = throw NotImplementedError("String does not support operation: unaryMinus")
    override fun minus(other: RefinedValue): StringValue =
        throw NotImplementedError("String does not support operation: minus")

    override fun compareTo(other: RefinedValue): Int =
        if (other is StringValue) value.compareTo(other.value) else throw IncompatibleTypesException(this, other)

    override fun plus(other: RefinedValue): StringValue = StringValue(value + other.value)
}
