package org.david.sessionkotlin.dsl.exception

public class InvalidCaseLabelException(label: String) : SessionKotlinDSLException("Case label $label is invalid.")
