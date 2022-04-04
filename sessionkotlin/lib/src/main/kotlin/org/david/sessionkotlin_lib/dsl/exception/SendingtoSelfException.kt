package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.Role

public class SendingtoSelfException(role: Role) : SessionKotlinDSLException("Cannot send to self (role $role)")
