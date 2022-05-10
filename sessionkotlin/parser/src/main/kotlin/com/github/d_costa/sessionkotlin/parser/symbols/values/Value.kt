package com.github.d_costa.sessionkotlin.parser.symbols.values

public abstract class Value(public open val value: Any) {
    public abstract operator fun unaryMinus(): Value
    public abstract operator fun compareTo(other: Value): Int
    public abstract operator fun minus(other: Value): Value
    public abstract operator fun plus(other: Value): Value
}
