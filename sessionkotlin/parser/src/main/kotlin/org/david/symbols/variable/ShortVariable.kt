package org.david.symbols.variable

import org.david.parser.exception.IncompatibleTypesException

public fun Short.toVar(): ShortVariable = ShortVariable(this)

public data class ShortVariable(override val value: Short) : Variable(value) {
    override fun unaryMinus(): IntVariable = IntVariable(value.unaryMinus())
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
            is ByteVariable -> IntVariable(value - other.value)
            is ShortVariable -> IntVariable(value - other.value)
            is IntVariable -> IntVariable(value - other.value)
            is LongVariable -> LongVariable(value - other.value)
            is FloatVariable -> FloatVariable(value - other.value)
            is DoubleVariable -> DoubleVariable(value - other.value)
            else -> throw IncompatibleTypesException(this, other)
        }

    override fun plus(other: Variable): Variable =
        when (other) {
            is ByteVariable -> IntVariable(value + other.value)
            is ShortVariable -> IntVariable(value + other.value)
            is IntVariable -> IntVariable(value + other.value)
            is LongVariable -> LongVariable(value + other.value)
            is FloatVariable -> FloatVariable(value + other.value)
            is DoubleVariable -> DoubleVariable(value + other.value)
            else -> throw IncompatibleTypesException(this, other)
        }
}
