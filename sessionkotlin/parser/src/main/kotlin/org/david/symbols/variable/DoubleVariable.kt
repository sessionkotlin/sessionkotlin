package org.david.symbols.variable

import org.david.parser.exception.IncompatibleTypesException

public fun Double.toVar(): DoubleVariable = DoubleVariable(this)

public data class DoubleVariable(override val value: Double) : Variable(value) {
    override fun unaryMinus(): DoubleVariable = DoubleVariable(value.unaryMinus())
    override fun compareTo(other: Variable): Int =
        when (other) {
            is ByteVariable -> value.compareTo(other.value)
            is ShortVariable -> value.compareTo(other.value)
            is IntVariable -> value.compareTo(other.value)
            is LongVariable -> value.compareTo(other.value)
            is FloatVariable -> value.compareTo(other.value)
            is DoubleVariable -> value.compareTo(other.value)
            else -> throw IncompatibleTypesException(this, other)
        }

    override fun minus(other: Variable): DoubleVariable =
        when (other) {
            is ByteVariable -> DoubleVariable(value - other.value)
            is ShortVariable -> DoubleVariable(value - other.value)
            is IntVariable -> DoubleVariable(value - other.value)
            is LongVariable -> DoubleVariable(value - other.value)
            is FloatVariable -> DoubleVariable(value - other.value)
            is DoubleVariable -> DoubleVariable(value - other.value)
            else -> throw IncompatibleTypesException(this, other)
        }

    override fun plus(other: Variable): DoubleVariable =
        when (other) {
            is ByteVariable -> DoubleVariable(value + other.value)
            is ShortVariable -> DoubleVariable(value + other.value)
            is IntVariable -> DoubleVariable(value + other.value)
            is LongVariable -> DoubleVariable(value + other.value)
            is FloatVariable -> DoubleVariable(value + other.value)
            is DoubleVariable -> DoubleVariable(value + other.value)
            else -> throw IncompatibleTypesException(this, other)
        }
}
