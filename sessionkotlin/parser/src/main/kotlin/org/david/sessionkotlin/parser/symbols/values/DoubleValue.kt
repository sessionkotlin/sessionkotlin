package org.david.sessionkotlin.parser.symbols.values

import org.david.sessionkotlin.parser.exception.IncompatibleTypesException

public fun Double.toVal(): DoubleValue = DoubleValue(this)

public data class DoubleValue(override val value: Double) : Value(value) {
    override fun unaryMinus(): DoubleValue = DoubleValue(value.unaryMinus())
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

    override fun minus(other: Value): DoubleValue =
        when (other) {
            is ByteValue -> DoubleValue(value - other.value)
            is ShortValue -> DoubleValue(value - other.value)
            is IntValue -> DoubleValue(value - other.value)
            is LongValue -> DoubleValue(value - other.value)
            is FloatValue -> DoubleValue(value - other.value)
            is DoubleValue -> DoubleValue(value - other.value)
            else -> throw IncompatibleTypesException(this, other)
        }

    override fun plus(other: Value): DoubleValue =
        when (other) {
            is ByteValue -> DoubleValue(value + other.value)
            is ShortValue -> DoubleValue(value + other.value)
            is IntValue -> DoubleValue(value + other.value)
            is LongValue -> DoubleValue(value + other.value)
            is FloatValue -> DoubleValue(value + other.value)
            is DoubleValue -> DoubleValue(value + other.value)
            else -> throw IncompatibleTypesException(this, other)
        }
}
