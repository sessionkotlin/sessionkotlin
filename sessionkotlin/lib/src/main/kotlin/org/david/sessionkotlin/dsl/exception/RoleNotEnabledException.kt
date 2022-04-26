package org.david.sessionkotlin.dsl.exception

import org.david.sessionkotlin.dsl.SKRole

public class RoleNotEnabledException(role: SKRole) : SessionKotlinDSLException("Role $role not enabled.")
