package org.david.sessionkotlin_lib.dsl.exception

public class DuplicateCaseLabelException(label: String) : SessionKotlinException("Case label $label is not unique.")
