package org.david.sessionkotlin.dsl.exception

public class InvalidBranchLabelException(label: String) : SessionKotlinDSLException("Branch label $label is invalid.")
