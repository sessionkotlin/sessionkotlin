package sessionkotlin.dsl

open class SKException(msg: String): RuntimeException(msg)

class RoleNotEnabledException(role: Role): SKException("Role $role not enabled.")

class SendingtoSelfException(role: Role): SKException("Cannot send to self (role $role)")
