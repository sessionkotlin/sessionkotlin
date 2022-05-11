package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.SKRole

public class SendingToSelfException(role: SKRole) : SessionKotlinDSLException("Cannot send to self (role $role)")
