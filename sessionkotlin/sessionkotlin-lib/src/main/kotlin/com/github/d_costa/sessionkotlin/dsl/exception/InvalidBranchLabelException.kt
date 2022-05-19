package com.github.d_costa.sessionkotlin.dsl.exception

/**
 * Thrown when attempting to use an invalid branch label.
 */
public class InvalidBranchLabelException(label: String) :
    SessionKotlinDSLException("Branch label $label is invalid. Labels cannot contain whitespace.")
