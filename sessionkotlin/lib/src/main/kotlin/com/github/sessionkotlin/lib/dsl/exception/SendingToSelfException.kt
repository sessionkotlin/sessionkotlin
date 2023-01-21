package com.github.sessionkotlin.lib.dsl.exception

import com.github.sessionkotlin.lib.dsl.SKRole

/**
 * Thrown when the message receiver is the sender.
 */
internal class SendingToSelfException(role: SKRole) : SessionKotlinDSLException("Cannot send to self (role $role)")
