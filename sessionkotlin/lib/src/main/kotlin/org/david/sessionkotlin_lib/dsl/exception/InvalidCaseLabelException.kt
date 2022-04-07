package org.david.sessionkotlin_lib.dsl.exception

public class InvalidCaseLabelException(label: String) : SessionKotlinDSLException("Case label $label is invalid.")
