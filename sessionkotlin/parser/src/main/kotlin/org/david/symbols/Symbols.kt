package org.david.symbols

internal sealed class Term {
    abstract fun value(bindings: Map<String, Int>): Int
    abstract fun names(): Set<String>
    fun compareTo(other: Term, bindings: Map<String, Int>): Int = value(bindings).compareTo(other.value(bindings))
}

public interface BooleanExpression {
    public fun value(bindings: Map<String, Int>): Boolean
    public fun names(): Set<String>
}

internal data class Name(val id: String) : Term() {
    override fun value(bindings: Map<String, Int>): Int = bindings.getValue(id)
    override fun names(): Set<String> = setOf(id)
}

internal data class Const(val value: Int) : Term() {
    override fun value(bindings: Map<String, Int>): Int = value
    override fun names(): Set<String> = emptySet()
}

internal data class Neg(val t: Term) : Term() {
    override fun value(bindings: Map<String, Int>): Int = -t.value(bindings)
    override fun names(): Set<String> = t.names()
}

internal data class Plus(val t1: Term, val t2: Term) : Term() {
    override fun value(bindings: Map<String, Int>): Int = t1.value(bindings) + t2.value(bindings)
    override fun names(): Set<String> = t1.names().plus(t2.names())
}

internal data class Minus(val t1: Term, val t2: Term) : Term() {
    override fun value(bindings: Map<String, Int>): Int = t1.value(bindings) - t2.value(bindings)
    override fun names(): Set<String> = t1.names().plus(t2.names())
}

internal data class Greater(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = e1.value(bindings) > e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

internal data class Lower(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = e1.value(bindings) < e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

internal data class GreaterEq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = e1.value(bindings) >= e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

internal data class LowerEq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = e1.value(bindings) <= e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

internal data class Eq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = e1.value(bindings) == e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

internal data class Neq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = e1.value(bindings) != e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

internal object True : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = true
    override fun names(): Set<String> = emptySet()
}

internal object False : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = false
    override fun names(): Set<String> = emptySet()
}

internal data class Not(val cond: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = !cond.value(bindings)
    override fun names(): Set<String> = cond.names()
}

internal data class And(val c1: BooleanExpression, val c2: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = c1.value(bindings) && c2.value(bindings)
    override fun names(): Set<String> = c1.names().plus(c2.names())
}

internal data class Or(val c1: BooleanExpression, val c2: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = c1.value(bindings) || c2.value(bindings)
    override fun names(): Set<String> = c1.names().plus(c2.names())
}
