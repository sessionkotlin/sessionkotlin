package com.github.d_costa.sessionkotlin.dsl.exception

public class DuplicateMessageLabelException(label: String) :
    SessionKotlinDSLException("Message label $label is not unique.")
