package com.github.d_costa.sessionkotlin.parser

import com.github.d_costa.sessionkotlin.parser.exception.ParsingException
import com.github.d_costa.sessionkotlin.parser.symbols.BooleanExpression
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException

/**
 * Helper object for parsing refinement expressions.
 */
public object RefinementParser {

    /**
     * Parse [input] with the refinement grammar.
     */
    public fun parseToEnd(input: String): BooleanExpression =
        try {
            grammar.parseToEnd(input)
        } catch (e: ParseException) {
            throw ParsingException(input)
        }
}
