package com.github.d_costa.sessionkotlin.parser

import com.github.d_costa.sessionkotlin.parser.symbols.BooleanExpression

public data class RefinementCondition(val plain: String, val expression: BooleanExpression)
