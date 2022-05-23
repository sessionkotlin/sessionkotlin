package com.github.d_costa.sessionkotlin.api.exception

/**
 * Thrown when a message is required to have a label.
 */
public class NoMessageLabelException : SessionKotlinAPIException("Message labels are required to generate the callbacks API")
