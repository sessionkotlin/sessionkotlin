package com.github.d_costa.sessionkotlin.parser.symbols.values

import com.github.d_costa.sessionkotlin.parser.exception.IncompatibleTypesException

public fun Long.toVal(): LongValue = LongValue(this)

public data class LongValue(override val value: Long) : Value(value) {
    override fun unaryMinus(): LongValue = LongValue(value.unaryMinus())
    override fun compareTo(other: Value): Int =
        when (other) {
            is ByteValue -> value.compareTo(other.value)
            is ShortValue -> value.compareTo(other.value)
            is IntValue -> value.compareTo(other.value)
            is LongValue -> value.compareTo(other.value)
            is FloatValue -> value.compareTo(other.value)
            is DoubleValue -> value.compareTo(other.value)
            else -> throw IncompatibleTypesException(this, other)
        }

    override fun minus(other: Value): Value =
        when (other) {
            is ByteValue -> LongValue(value - other.value)
            is ShortValue -> LongValue(value - other.value)
            is IntValue -> LongValue(value - other.value)
            is LongValue -> LongValue(value - other.value)
            is FloatValue -> FloatValue(value - other.value)
            is DoubleValue -> DoubleValue(value - other.value)
            else -> throw IncompatibleTypesException(this, other)
        }

    override fun plus(other: Value): Value =
        when (other) {
            is ByteValue -> LongValue(value + other.value)
            is ShortValue -> LongValue(value + other.value)
            is IntValue -> LongValue(value + other.value)
            is LongValue -> LongValue(value + other.value)
            is FloatValue -> FloatValue(value + other.value)
            is DoubleValue -> DoubleValue(value + other.value)
            else -> throw IncompatibleTypesException(this, other)
        }
}
