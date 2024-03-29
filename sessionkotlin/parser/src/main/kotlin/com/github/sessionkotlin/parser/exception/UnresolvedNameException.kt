package com.github.sessionkotlin.parser.exception

/**
 * Thrown when an expression references a name that was not previously declared.
 */
public class UnresolvedNameException(name: String) : SessionKotlinParserException("Unresolved name: $name")
