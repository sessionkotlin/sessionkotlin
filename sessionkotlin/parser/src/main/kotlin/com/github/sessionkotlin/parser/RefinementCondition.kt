package com.github.sessionkotlin.parser

import com.github.sessionkotlin.parser.symbols.BooleanExpression

public data class RefinementCondition(val plain: String, val expression: BooleanExpression)
