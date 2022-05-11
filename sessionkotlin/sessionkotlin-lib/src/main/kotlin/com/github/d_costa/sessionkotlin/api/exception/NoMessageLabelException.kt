package com.github.d_costa.sessionkotlin.api.exception

public open class NoMessageLabelException(msg: String) : SessionKotlinAPIException("Message $msg requires a label to generate the callbacks API")
