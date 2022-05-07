package org.david.sessionkotlin.parser.exception

public class UnresolvedNameException(name: String) : SessionKotlinParserException("Unresolved name: $name")
