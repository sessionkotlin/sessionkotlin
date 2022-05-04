package org.david.symbols

internal sealed class Term {
    abstract fun value(bindings: Map<String, Int>): Int
    fun compareTo(other: Term, bindings: Map<String, Int>): Int = value(bindings).compareTo(other.value(bindings))
}

internal interface BooleanExpression {
    fun value(bindings: Map<String, Int>): Boolean
}

internal data class Name(val id: String) : Term() {
    override fun value(bindings: Map<String, Int>): Int = bindings.getValue(id)
}

internal data class Const(val value: Int) : Term() {
    override fun value(bindings: Map<String, Int>): Int = value
}

internal data class Neg(val t: Term) : Term() {
    override fun value(bindings: Map<String, Int>): Int = -t.value(bindings)
}

internal data class Plus(val t1: Term, val t2: Term) : Term() {
    override fun value(bindings: Map<String, Int>): Int = t1.value(bindings) + t2.value(bindings)
}

internal data class Minus(val t1: Term, val t2: Term) : Term() {
    override fun value(bindings: Map<String, Int>): Int = t1.value(bindings) - t2.value(bindings)
}

internal data class Greater(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = e1.value(bindings) > e2.value(bindings)
}

internal data class Lower(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = e1.value(bindings) < e2.value(bindings)
}

internal data class GreaterEq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = e1.value(bindings) >= e2.value(bindings)
}

internal data class LowerEq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = e1.value(bindings) <= e2.value(bindings)
}

internal data class Eq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = e1.value(bindings) == e2.value(bindings)
}

internal data class Neq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = e1.value(bindings) != e2.value(bindings)
}

internal object True : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = true
}

internal object False : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = false
}

internal data class Not(val cond: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = !cond.value(bindings)
}

internal data class And(val c1: BooleanExpression, val c2: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = c1.value(bindings) && c2.value(bindings)
}

internal data class Or(val c1: BooleanExpression, val c2: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, Int>): Boolean = c1.value(bindings) || c2.value(bindings)
}
