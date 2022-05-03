package org.david.symbols

internal interface RefinementExpression
//internal interface BooleanExpr : RefinementExpression
internal interface Term

internal data class Name(val id: String) : Term
internal data class Const(val value: Int) : Term

internal data class Neg(val t: Term) : Term
internal data class Plus(val t1: Term, val t2: Term) : Term
internal data class Minus(val t1: Term, val t2: Term) : Term

internal data class Greater(val e1: Term, val e2: Term) : Term
internal data class Lower(val e1: Term, val e2: Term) : Term
internal data class GreaterEq(val e1: Term, val e2: Term) : Term
internal data class LowerEq(val e1: Term, val e2: Term) : Term
internal data class Eq(val e1: Term, val e2: Term) : Term
internal data class Neq(val e1: Term, val e2: Term) : Term

internal data class Not(val cond: Term) : Term
internal data class And(val c1: Term, val c2: Term) : Term
internal data class Or(val c1: Term, val c2: Term) : Term
