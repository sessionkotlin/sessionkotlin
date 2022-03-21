package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.Role

class RoleNotEnabledException(role: Role) : SessionKotlinException("Role $role not enabled.")
