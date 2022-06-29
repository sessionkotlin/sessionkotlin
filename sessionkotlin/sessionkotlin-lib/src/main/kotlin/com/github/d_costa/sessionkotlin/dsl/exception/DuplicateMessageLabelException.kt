package com.github.d_costa.sessionkotlin.dsl.exception

/**
 * Thrown when attempting to reuse a message label.
 */
public class DuplicateMessageLabelException(labels: Collection<String>) :
    SessionKotlinDSLException("Callbacks API generation is enabled and some message labels are not unique: ${labels.joinToString { "," }}")
