package org.david.parser.exception

internal class IncompatibleTypesException(a: Any, b: Any) :
    SessionKotlinParserException("Incompatible types: ${a.javaClass}, ${b.javaClass}")
