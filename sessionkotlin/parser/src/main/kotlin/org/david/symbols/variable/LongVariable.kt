package org.david.symbols.variable

import org.david.parser.exception.IncompatibleTypesException

public fun Long.toVar(): LongVariable = LongVariable(this)

public data class LongVariable(override val value: Long) : Variable(value) {
    override fun unaryMinus(): LongVariable = LongVariable(value.unaryMinus())
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
            is ByteVariable -> LongVariable(value - other.value)
            is ShortVariable -> LongVariable(value - other.value)
            is IntVariable -> LongVariable(value - other.value)
            is LongVariable -> LongVariable(value - other.value)
            is FloatVariable -> FloatVariable(value - other.value)
            is DoubleVariable -> DoubleVariable(value - other.value)
            else -> throw IncompatibleTypesException(this, other)
        }

    override fun plus(other: Variable): Variable =
        when (other) {
            is ByteVariable -> LongVariable(value + other.value)
            is ShortVariable -> LongVariable(value + other.value)
            is IntVariable -> LongVariable(value + other.value)
            is LongVariable -> LongVariable(value + other.value)
            is FloatVariable -> FloatVariable(value + other.value)
            is DoubleVariable -> DoubleVariable(value + other.value)
            else -> throw IncompatibleTypesException(this, other)
        }
}
