package com.github.sessionkotlin.parser.symbols.values

import com.github.sessionkotlin.parser.exception.IncompatibleTypesException

public fun Long.toVal(): IntegerValue = IntegerValue(this)
public fun Int.toVal(): IntegerValue = IntegerValue(this.toLong())
public fun Short.toVal(): IntegerValue = IntegerValue(this.toLong())
public fun Byte.toVal(): IntegerValue = IntegerValue(this.toLong())

/**
 * API for integer values.
 */
public data class IntegerValue(override val value: Long) : RefinedValue(value) {
    override fun unaryMinus(): IntegerValue = IntegerValue(value.unaryMinus())
    override fun compareTo(other: RefinedValue): Int =
        when (other) {
            is IntegerValue -> value.compareTo(other.value)
            is RealValue -> value.compareTo(other.value)
            else -> throw IncompatibleTypesException(this, other)
        }

    override fun minus(other: RefinedValue): RefinedValue =
        when (other) {
            is IntegerValue -> IntegerValue(value - other.value)
            is RealValue -> RealValue(value - other.value)
            else -> throw IncompatibleTypesException(this, other)
        }

    override fun plus(other: RefinedValue): RefinedValue =
        when (other) {
            is IntegerValue -> IntegerValue(value + other.value)
            is RealValue -> RealValue(value + other.value)
            else -> throw IncompatibleTypesException(this, other)
        }
}
