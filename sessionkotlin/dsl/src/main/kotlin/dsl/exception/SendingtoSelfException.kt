package dsl.exception

import dsl.Role

class SendingtoSelfException(role: Role) : SessionKotlinException("Cannot send to self (role $role)")
