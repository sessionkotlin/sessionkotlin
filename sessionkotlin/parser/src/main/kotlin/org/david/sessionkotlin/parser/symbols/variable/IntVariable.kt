package org.david.sessionkotlin.parser.symbols.variable

import org.david.sessionkotlin.parser.exception.IncompatibleTypesException

public fun Int.toVar(): IntVariable = IntVariable(this)

public data class IntVariable(public override val value: Int) : Variable(value) {
    override fun unaryMinus(): Variable = IntVariable(value.unaryMinus())
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
