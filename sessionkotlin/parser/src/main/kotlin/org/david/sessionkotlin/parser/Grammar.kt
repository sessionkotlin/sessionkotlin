package org.david.sessionkotlin.parser

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.lexer.literalToken
import com.github.h0tk3y.betterParse.lexer.regexToken
import com.github.h0tk3y.betterParse.parser.Parser
import org.david.sessionkotlin.parser.symbols.*
import org.david.sessionkotlin.parser.symbols.variable.toVar

internal val grammar: Grammar<BooleanExpression> = object : Grammar<BooleanExpression>() {
    val lTrue by literalToken("true")
    val lFalse by literalToken("false")
    val float by regexToken("(\\d+[Ff])|(\\.\\d+[Ff])|(\\d+\\.\\d+[Ff])")
    val double by regexToken("(\\.\\d+)|(\\d+\\.\\d+)")
    val long by regexToken("\\d+L")
    val integer by regexToken("\\d+")
    val word by regexToken("\\w+")
    val plus by literalToken("+")
    val impl by literalToken("->")
    val minus by literalToken("-")
    val eq by literalToken("==")
    val neq by literalToken("!=")
    val lwrEq by literalToken("<=")
    val lwr by literalToken("<")
    val gtEq by literalToken(">=")
    val gt by literalToken(">")
    val not by literalToken("!")
    val and by literalToken("&&")
    val or by literalToken("||")
    val ws by regexToken("\\s+", ignore = true)
    val lpar by literalToken("(")
    val rpar by literalToken(")")
    val singleQuote by literalToken("'")

    val term: Parser<Term> by
    (integer use { Const(text.toInt().toVar()) }) or
        (float use { Const(text.slice(0 until length - 1).toFloat().toVar()) }) or
        (double use { Const(text.toDouble().toVar()) }) or
        (long use { Const(text.slice(0 until length - 1).toLong().toVar()) }) or
        (long use { Const(text.slice(0 until length - 1).toLong().toVar()) }) or
        ((-singleQuote * word * -singleQuote) use { Const(text.toVar()) }) or
        (word use { Name(text) }) or
        -lpar * parser(::expr) * -rpar or
        (-minus * parser(::expr) map { Neg(it) })

    val expr: Parser<Term> =
        leftAssociative(term, plus or minus) { l, op, r -> if (op.type == plus) Plus(l, r) else Minus(l, r) }

    val booleanExpr: Parser<BooleanExpression> by
    (lTrue use { True }) or
        (lFalse use { False }) or
        ((expr * -eq * expr) use { Eq(t1, t2) }) or
        ((expr * -neq * expr) use { Neq(t1, t2) }) or
        ((expr * -lwr * expr) use { Lower(t1, t2) }) or
        ((expr * -lwrEq * expr) use { LowerEq(t1, t2) }) or
        ((expr * -gt * expr) use { Greater(t1, t2) }) or
        ((expr * -gtEq * expr) use { GreaterEq(t1, t2) }) or
        ((expr * -lwrEq * expr) use { LowerEq(t1, t2) }) or
        (-not * parser(::booleanExpr) map { Not(it) }) or
        -lpar * parser(::rootParser) * -rpar

    val andChain: Parser<BooleanExpression> by leftAssociative(booleanExpr, and) { l, _, r -> And(l, r) }
    val orChain: Parser<BooleanExpression> by leftAssociative(andChain, or) { l, _, r -> Or(l, r) }
    val implication: Parser<BooleanExpression> by rightAssociative(orChain, impl) { l, _, r -> Impl(l, r) }

    override val rootParser: Parser<BooleanExpression> by implication
}

public object RefinementParser {
    public fun parseToEnd(input: String): BooleanExpression = grammar.parseToEnd(input)
}
