package com.github.d_costa.sessionkotlin.dsl.exception

public class DuplicateMessageLabelsException(labels: Iterable<String>) :
    SessionKotlinDSLException("Message labels $labels are not unique and callbacks API generation was requested.")
