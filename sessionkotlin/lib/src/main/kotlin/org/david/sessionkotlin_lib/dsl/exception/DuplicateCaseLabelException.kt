package org.david.sessionkotlin_lib.dsl.exception

class DuplicateCaseLabelException(label: String) : SessionKotlinException("Case label $label is not unique.")
