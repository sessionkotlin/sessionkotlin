package com.github.sessionkotlin.parser

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.parser.ParseException
import com.github.sessionkotlin.parser.exception.ParsingException
import com.github.sessionkotlin.parser.symbols.BooleanExpression

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
