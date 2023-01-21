package com.github.sessionkotlin.lib.dsl.exception

internal class DuplicateMessageLabelsException(labels: Iterable<String>) :
    SessionKotlinDSLException("Message labels $labels are not unique and callbacks API generation was requested.")
