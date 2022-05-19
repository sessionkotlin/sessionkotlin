package com.github.d_costa.sessionkotlin.parser.exception

/**
 * Thrown when there is an attempt to invoke an unsupported binary operation between two types.
 */
public class IncompatibleTypesException(a: Any, b: Any) :
    SessionKotlinParserException("Incompatible types: ${a.javaClass}, ${b.javaClass}")
