package org.david.parser.exception

internal class UnknownNumberClassException(n: Number) : SessionKotlinParserException("Unknown number class: ${n.javaClass}")
