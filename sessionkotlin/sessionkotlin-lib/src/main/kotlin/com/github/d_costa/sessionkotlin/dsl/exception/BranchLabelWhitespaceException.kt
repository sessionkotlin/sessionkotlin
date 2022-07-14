package com.github.d_costa.sessionkotlin.dsl.exception

/**
 * Thrown when attempting to use an invalid branch label.
 */
internal class BranchLabelWhitespaceException(label: String) :
    SessionKotlinDSLException("Branch label '$label' is invalid. Cannot contain whitespace.")
