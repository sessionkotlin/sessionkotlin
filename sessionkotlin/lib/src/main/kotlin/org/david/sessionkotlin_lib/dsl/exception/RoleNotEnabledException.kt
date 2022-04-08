package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.SKRole

public class RoleNotEnabledException(role: SKRole) : SessionKotlinDSLException("Role $role not enabled.")
