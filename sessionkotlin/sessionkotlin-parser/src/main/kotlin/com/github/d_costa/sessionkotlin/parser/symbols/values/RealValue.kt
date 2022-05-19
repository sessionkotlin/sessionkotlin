package com.github.d_costa.sessionkotlin.parser.symbols.values

import com.github.d_costa.sessionkotlin.parser.exception.IncompatibleTypesException

public fun Double.toVal(): RealValue = RealValue(this)

/**
 * API for real values.
 */
public data class RealValue(override val value: Double) : RefinedValue(value) {
    override fun unaryMinus(): RealValue = RealValue(value.unaryMinus())
    override fun compareTo(other: RefinedValue): Int =
        when (other) {
            is IntegerValue -> value.compareTo(other.value)
            is RealValue -> value.compareTo(other.value)
            else -> throw IncompatibleTypesException(this, other)
        }

    override fun minus(other: RefinedValue): RealValue =
        when (other) {
            is IntegerValue -> RealValue(value - other.value)
            is RealValue -> RealValue(value - other.value)
            else -> throw IncompatibleTypesException(this, other)
        }

    override fun plus(other: RefinedValue): RealValue =
        when (other) {
            is IntegerValue -> RealValue(value + other.value)
            is RealValue -> RealValue(value + other.value)
            else -> throw IncompatibleTypesException(this, other)
        }
}
