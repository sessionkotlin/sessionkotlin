package dsl.exception

import dsl.Role

class RoleNotEnabledException(role: Role) : SessionKotlinException("Role $role not enabled.")
