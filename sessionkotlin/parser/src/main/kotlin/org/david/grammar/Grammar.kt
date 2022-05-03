package org.david.grammar

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import org.david.symbols.*

internal val grammar = object : Grammar<Term>() {
    val number by regexToken("\\d+")
    val id by regexToken("\\w+")
    val plus by literalToken("+")
    val minus by literalToken("-")
    val eq by literalToken("=")
    val neq by literalToken("!=")
    val lwrEq by literalToken("<=")
    val lwr by literalToken("<")
    val gtEq by literalToken(">=")
    val gt by literalToken(">")
    val not by literalToken("!")
    val and by literalToken("&")
    val or by literalToken("|")
    val ws by regexToken("\\s+", ignore = true)
    val lpar by literalToken("(")
    val rpar by literalToken(")")

    val term: Parser<Term> by
    (number use { Const(text.toInt()) }) or
            (id use { Name(text) }) or
            -lpar * parser(::rootParser) * -rpar or
            (-minus * parser(::expr) map { Neg(it) })

    val expr: Parser<Term> = leftAssociative(term, plus or minus)
    { l, op, r -> if (op.type == plus) Plus(l, r) else Minus(l, r) }

    val booleanExpr: Parser<Term> by
    ((expr * -eq * expr) use { Eq(t1, t2) }) or
            ((expr * -neq * expr) use { Neq(t1, t2) }) or
            ((expr * -lwr * expr) use { Lower(t1, t2) }) or
            ((expr * -lwrEq * expr) use { LowerEq(t1, t2) }) or
            ((expr * -gt * expr) use { Greater(t1, t2) }) or
            ((expr * -gtEq * expr) use { GreaterEq(t1, t2) }) or
            ((expr * -lwrEq * expr) use { LowerEq(t1, t2) }) or
            (-not * parser(::booleanExpr) map { Not(it) }) or
            expr

    val andChain by leftAssociative(booleanExpr, and) { l, _, r -> And(l, r) }
    val orChain by leftAssociative(andChain, or) { l, _, r -> Or(l, r) }

    override val rootParser by orChain
}
