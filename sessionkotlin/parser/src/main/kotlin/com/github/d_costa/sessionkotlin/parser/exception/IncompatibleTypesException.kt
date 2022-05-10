package com.github.d_costa.sessionkotlin.parser.exception

public class IncompatibleTypesException(a: Any, b: Any) :
    SessionKotlinParserException("Incompatible types: ${a.javaClass}, ${b.javaClass}")
