package sessionkotlin.dsl.exception

import sessionkotlin.dsl.Role

class SendingtoSelfException(role: Role) : SessionKotlinException("Cannot send to self (role $role)")
