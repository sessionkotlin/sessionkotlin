package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.SKRole

public class RoleNotEnabledException(role: SKRole) : SessionKotlinDSLException("Role $role not enabled.")
