package com.github.d_costa.sessionkotlin.parser.symbols.values

import com.github.d_costa.sessionkotlin.parser.exception.IncompatibleTypesException

public fun String.toVal(): StringValue = StringValue(this)

public data class StringValue(override val value: String) : Value(value) {
    override fun unaryMinus(): Value = throw NotImplementedError("String does not support operation: unaryMinus")
    override fun minus(other: Value): StringValue =
        throw NotImplementedError("String does not support operation: minus")

    override fun compareTo(other: Value): Int =
        if (other is StringValue) value.compareTo(other.value) else throw IncompatibleTypesException(this, other)

    override fun plus(other: Value): StringValue = StringValue(value + other.value)
}
