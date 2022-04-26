package org.david.sessionkotlin.dsl.exception

import org.david.sessionkotlin.dsl.SKRole

public class SendingToSelfException(role: SKRole) : SessionKotlinDSLException("Cannot send to self (role $role)")
