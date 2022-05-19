package com.github.d_costa.sessionkotlin.dsl.exception

/**
 * Thrown when attempting to reuse a branch label.
 */
public class DuplicateBranchLabelException(label: String) :
    SessionKotlinDSLException("Branch label $label is not unique.")
