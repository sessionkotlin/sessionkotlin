package org.david.parser.exception

internal class UnresolvedNameException(name: String) : SessionKotlinParserException("Unresolved name: $name")
