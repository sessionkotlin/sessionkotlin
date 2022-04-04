package org.david.sessionkotlin_lib.dsl.exception

public class DuplicateCaseLabelException(label: String) : SessionKotlinDSLException("Case label $label is not unique.")
