package com.github.d_costa.sessionkotlin.parser.symbols.values

/**
 * API for refinement values.
 *
 */
public abstract class RefinedValue(public open val value: Any) {
    public abstract operator fun unaryMinus(): RefinedValue
    public abstract operator fun compareTo(other: RefinedValue): Int
    public abstract operator fun minus(other: RefinedValue): RefinedValue
    public abstract operator fun plus(other: RefinedValue): RefinedValue
}
