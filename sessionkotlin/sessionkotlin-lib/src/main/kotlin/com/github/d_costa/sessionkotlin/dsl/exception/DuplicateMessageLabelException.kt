package com.github.d_costa.sessionkotlin.dsl.exception

/**
 * Thrown when attempting to reuse a message label.
 */
public class DuplicateMessageLabelException(label: String) :
    SessionKotlinDSLException("Message label '$label' is not unique.")
