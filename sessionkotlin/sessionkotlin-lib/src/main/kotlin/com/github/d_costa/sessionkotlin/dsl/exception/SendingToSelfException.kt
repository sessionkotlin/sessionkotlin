package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.SKRole

/**
 * Thrown when the message receiver is the sender.
 */
public class SendingToSelfException(role: SKRole) : SessionKotlinDSLException("Cannot send to self (role $role)")
