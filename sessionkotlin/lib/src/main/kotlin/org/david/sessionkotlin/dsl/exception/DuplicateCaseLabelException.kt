package org.david.sessionkotlin.dsl.exception

public class DuplicateCaseLabelException(label: String) : SessionKotlinDSLException("Case label $label is not unique.")
