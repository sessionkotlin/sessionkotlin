package com.github.d_costa.sessionkotlin.dsl.exception

public class DuplicateBranchLabelException(label: String) :
    SessionKotlinDSLException("Branch label $label is not unique.")
