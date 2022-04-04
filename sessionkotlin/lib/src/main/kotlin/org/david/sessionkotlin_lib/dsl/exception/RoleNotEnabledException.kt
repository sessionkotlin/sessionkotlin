package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.Role

public class RoleNotEnabledException(role: Role) : SessionKotlinDSLException("Role $role not enabled.")
