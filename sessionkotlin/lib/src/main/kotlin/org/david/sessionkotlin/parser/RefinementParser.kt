package org.david.sessionkotlin.parser

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.david.parser.grammar
import org.david.symbols.BooleanExpression

public object RefinementParser {
    public fun parseToEnd(input: String): BooleanExpression = grammar.parseToEnd(input)
}
