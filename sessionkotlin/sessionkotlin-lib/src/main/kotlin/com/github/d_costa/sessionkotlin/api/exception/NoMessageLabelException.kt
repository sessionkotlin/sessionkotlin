package com.github.d_costa.sessionkotlin.api.exception

/**
 * Thrown when a message is required to have a label.
 */
public class NoMessageLabelException(msg: String) : SessionKotlinAPIException("Message $msg requires a label to generate the callbacks API")
