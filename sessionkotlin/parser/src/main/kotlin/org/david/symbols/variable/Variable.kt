package org.david.symbols.variable

public abstract class Variable(public open val value: Any) {
    public abstract operator fun unaryMinus(): Variable
    public abstract operator fun compareTo(other: Variable): Int
    public abstract operator fun minus(other: Variable): Variable
    public abstract operator fun plus(other: Variable): Variable
}
