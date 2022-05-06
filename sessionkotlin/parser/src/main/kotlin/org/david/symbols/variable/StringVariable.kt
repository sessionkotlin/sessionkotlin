package org.david.symbols.variable

import org.david.parser.exception.IncompatibleTypesException

public fun String.toVar(): StringVariable = StringVariable(this)

public data class StringVariable(override val value: String) : Variable(value) {
    override fun unaryMinus(): Variable = throw NotImplementedError("String does not support operation: unaryMinus")
    override fun minus(other: Variable): StringVariable =
        throw NotImplementedError("String does not support operation: unaryMinus")

    override fun compareTo(other: Variable): Int =
        if (other is StringVariable) value.compareTo(other.value) else throw IncompatibleTypesException(this, other)

    override fun plus(other: Variable): StringVariable = StringVariable(value + other.value)
}
