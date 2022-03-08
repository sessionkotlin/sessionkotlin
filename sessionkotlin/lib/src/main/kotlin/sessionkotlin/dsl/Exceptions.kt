package sessionkotlin.dsl

open class SKException(msg: String): RuntimeException(msg)

class RoleInCaseNotEnabledException(caseLabel: String, role: Role): SKException("Role $role not enabled in case $caseLabel")

class SendingtoSelfException(role: Role): SKException("Cannot send to self (role $role)")
