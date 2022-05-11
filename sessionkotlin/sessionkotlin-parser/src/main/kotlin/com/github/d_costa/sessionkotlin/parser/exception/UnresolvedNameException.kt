package com.github.d_costa.sessionkotlin.parser.exception

public class UnresolvedNameException(name: String) : SessionKotlinParserException("Unresolved name: $name")
