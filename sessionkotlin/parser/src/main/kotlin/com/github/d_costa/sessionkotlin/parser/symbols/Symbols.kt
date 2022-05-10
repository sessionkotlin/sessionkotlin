package com.github.d_costa.sessionkotlin.parser.symbols

import com.github.d_costa.sessionkotlin.parser.exception.UnresolvedNameException
import com.github.d_costa.sessionkotlin.parser.symbols.values.RefinedValue
import com.github.d_costa.sessionkotlin.parser.symbols.values.toVal
import com.github.d_costa.sessionkotlin.parser.util.getOrThrow

public sealed interface Term {
    public fun value(bindings: Map<String, RefinedValue>): RefinedValue
    public fun names(): Set<String>
}

public data class Name(val id: String) : Term {
    override fun value(bindings: Map<String, RefinedValue>): RefinedValue =
        bindings.getOrThrow(id) { UnresolvedNameException(id) }

    override fun names(): Set<String> = setOf(id)
}

public data class Const(val v: RefinedValue) : Term {
    override fun value(bindings: Map<String, RefinedValue>): RefinedValue = v
    override fun names(): Set<String> = emptySet()
}

internal fun cLong(v: Long) = Const(v.toVal())
internal fun cLong(v: Int) = Const(v.toLong().toVal())

internal fun cDouble(v: Double) = Const(v.toVal())

internal fun cString(v: String) = Const(v.toVal())

public data class Neg(val t: Term) : Term {
    override fun value(bindings: Map<String, RefinedValue>): RefinedValue = -t.value(bindings)
    override fun names(): Set<String> = t.names()
}

public data class Plus(val t1: Term, val t2: Term) : Term {
    override fun value(bindings: Map<String, RefinedValue>): RefinedValue = t1.value(bindings) + t2.value(bindings)
    override fun names(): Set<String> = t1.names().plus(t2.names())
}

public data class Minus(val t1: Term, val t2: Term) : Term {
    override fun value(bindings: Map<String, RefinedValue>): RefinedValue = t1.value(bindings) - t2.value(bindings)
    override fun names(): Set<String> = t1.names().plus(t2.names())
}

public sealed interface BooleanExpression {
    public fun value(bindings: Map<String, RefinedValue>): Boolean
    public fun names(): Set<String>
}

public data class Greater(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, RefinedValue>): Boolean = e1.value(bindings) > e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

public data class Lower(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, RefinedValue>): Boolean = e1.value(bindings) < e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

public data class GreaterEq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, RefinedValue>): Boolean = e1.value(bindings) >= e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

public data class LowerEq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, RefinedValue>): Boolean = e1.value(bindings) <= e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

public data class Eq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, RefinedValue>): Boolean = e1.value(bindings).compareTo(e2.value(bindings)) == 0
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

public data class Neq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, RefinedValue>): Boolean = e1.value(bindings).compareTo(e2.value(bindings)) != 0
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

public object True : BooleanExpression {
    override fun value(bindings: Map<String, RefinedValue>): Boolean = true
    override fun names(): Set<String> = emptySet()
}

public object False : BooleanExpression {
    override fun value(bindings: Map<String, RefinedValue>): Boolean = false
    override fun names(): Set<String> = emptySet()
}

public data class Not(val cond: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, RefinedValue>): Boolean = !cond.value(bindings)
    override fun names(): Set<String> = cond.names()
}

public data class Impl(val c1: BooleanExpression, val c2: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, RefinedValue>): Boolean = !c1.value(bindings) || c2.value(bindings)
    override fun names(): Set<String> = c1.names().plus(c2.names())
}

public data class And(val c1: BooleanExpression, val c2: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, RefinedValue>): Boolean = c1.value(bindings) && c2.value(bindings)
    override fun names(): Set<String> = c1.names().plus(c2.names())
}

public data class Or(val c1: BooleanExpression, val c2: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, RefinedValue>): Boolean = c1.value(bindings) || c2.value(bindings)
    override fun names(): Set<String> = c1.names().plus(c2.names())
}
