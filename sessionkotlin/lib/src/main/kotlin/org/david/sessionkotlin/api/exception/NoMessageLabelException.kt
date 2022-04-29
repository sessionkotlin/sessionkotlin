package org.david.sessionkotlin.api.exception

public open class NoMessageLabelException(msg: String) : SessionKotlinAPIException("Message $msg requires a label to generate the callbacks API")
