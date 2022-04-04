package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.Role

public class SendingtoSelfException(role: Role) : SessionKotlinException("Cannot send to self (role $role)")
