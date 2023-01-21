package com.github.sessionkotlin.parser.exception

public class ParsingException(expression: String) :
    SessionKotlinParserException("Error parsing an expression [$expression]")
