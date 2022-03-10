package sessionkotlin.dsl.exception

import sessionkotlin.dsl.Role

class RoleNotEnabledException(role: Role): SessionKotlinException("Role $role not enabled.")
