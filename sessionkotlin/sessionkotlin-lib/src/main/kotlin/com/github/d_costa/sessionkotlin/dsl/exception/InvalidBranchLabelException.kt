package com.github.d_costa.sessionkotlin.dsl.exception

public class InvalidBranchLabelException(label: String) : SessionKotlinDSLException("Branch label $label is invalid.")
