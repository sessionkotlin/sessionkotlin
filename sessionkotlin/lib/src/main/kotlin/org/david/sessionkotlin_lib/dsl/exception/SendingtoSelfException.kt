package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.SKRole

public class SendingtoSelfException(role: SKRole) : SessionKotlinDSLException("Cannot send to self (role $role)")
