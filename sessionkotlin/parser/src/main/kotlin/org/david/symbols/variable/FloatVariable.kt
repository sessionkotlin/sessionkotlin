package org.david.symbols.variable

import org.david.parser.exception.IncompatibleTypesException

public fun Float.toVar(): FloatVariable = FloatVariable(this)

public data class FloatVariable(override val value: Float) : Variable(value) {
    override fun unaryMinus(): FloatVariable = FloatVariable(value.unaryMinus())
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

    override fun minus(other: Variable): Variable =
        when (other) {
            is ByteVariable -> FloatVariable(value - other.value)
            is ShortVariable -> FloatVariable(value - other.value)
            is IntVariable -> FloatVariable(value - other.value)
            is LongVariable -> FloatVariable(value - other.value)
            is FloatVariable -> FloatVariable(value - other.value)
            is DoubleVariable -> DoubleVariable(value - other.value)
            else -> throw IncompatibleTypesException(this, other)
        }

    override fun plus(other: Variable): Variable =
        when (other) {
            is ByteVariable -> FloatVariable(value + other.value)
            is ShortVariable -> FloatVariable(value + other.value)
            is IntVariable -> FloatVariable(value + other.value)
            is LongVariable -> FloatVariable(value + other.value)
            is FloatVariable -> FloatVariable(value + other.value)
            is DoubleVariable -> DoubleVariable(value + other.value)
            else -> throw IncompatibleTypesException(this, other)
        }
}
