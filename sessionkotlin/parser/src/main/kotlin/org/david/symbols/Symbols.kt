package org.david.symbols

import org.david.parser.exception.UnresolvedNameException
import org.david.parser.util.getOrThrow
import org.david.symbols.variable.Variable
import org.david.symbols.variable.toVar

internal sealed interface Term {
    fun value(bindings: Map<String, Variable>): Variable
    fun names(): Set<String>
}

internal data class Name(val id: String) : Term {
    override fun value(bindings: Map<String, Variable>): Variable =
        bindings.getOrThrow(id) { UnresolvedNameException(id) }

    override fun names(): Set<String> = setOf(id)
}

internal data class Const(val v: Variable) : Term {
    override fun value(bindings: Map<String, Variable>) = v
    override fun names(): Set<String> = emptySet()
}

internal fun cInt(v: Int) = Const(v.toVar())
internal fun cLong(v: Long) = Const(v.toVar())
internal fun cFloat(v: Float) = Const(v.toVar())
internal fun cDouble(v: Double) = Const(v.toVar())

internal data class Neg(val t: Term) : Term {
    override fun value(bindings: Map<String, Variable>): Variable = -t.value(bindings)
    override fun names(): Set<String> = t.names()
}

internal data class Plus(val t1: Term, val t2: Term) : Term {
    override fun value(bindings: Map<String, Variable>): Variable = t1.value(bindings) + t2.value(bindings)
    override fun names(): Set<String> = t1.names().plus(t2.names())
}

internal data class Minus(val t1: Term, val t2: Term) : Term {
    override fun value(bindings: Map<String, Variable>): Variable = t1.value(bindings) - t2.value(bindings)
    override fun names(): Set<String> = t1.names().plus(t2.names())
}

public sealed interface BooleanExpression {
    public fun value(bindings: Map<String, Variable>): Boolean
    public fun names(): Set<String>
}

internal data class Greater(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Variable>): Boolean = e1.value(bindings) > e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

internal data class Lower(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Variable>): Boolean = e1.value(bindings) < e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

internal data class GreaterEq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Variable>): Boolean = e1.value(bindings) >= e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

internal data class LowerEq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Variable>): Boolean = e1.value(bindings) <= e2.value(bindings)
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

internal data class Eq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Variable>): Boolean = e1.value(bindings).compareTo(e2.value(bindings)) == 0
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

internal data class Neq(val e1: Term, val e2: Term) : BooleanExpression {
    override fun value(bindings: Map<String, Variable>): Boolean = e1.value(bindings).compareTo(e2.value(bindings)) != 0
    override fun names(): Set<String> = e1.names().plus(e2.names())
}

internal object True : BooleanExpression {
    override fun value(bindings: Map<String, Variable>): Boolean = true
    override fun names(): Set<String> = emptySet()
}

internal object False : BooleanExpression {
    override fun value(bindings: Map<String, Variable>): Boolean = false
    override fun names(): Set<String> = emptySet()
}

internal data class Not(val cond: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, Variable>): Boolean = !cond.value(bindings)
    override fun names(): Set<String> = cond.names()
}

internal data class Impl(val c1: BooleanExpression, val c2: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, Variable>): Boolean = !c1.value(bindings) || c2.value(bindings)
    override fun names(): Set<String> = c1.names().plus(c2.names())
}

internal data class And(val c1: BooleanExpression, val c2: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, Variable>): Boolean = c1.value(bindings) && c2.value(bindings)
    override fun names(): Set<String> = c1.names().plus(c2.names())
}

internal data class Or(val c1: BooleanExpression, val c2: BooleanExpression) : BooleanExpression {
    override fun value(bindings: Map<String, Variable>): Boolean = c1.value(bindings) || c2.value(bindings)
    override fun names(): Set<String> = c1.names().plus(c2.names())
}
