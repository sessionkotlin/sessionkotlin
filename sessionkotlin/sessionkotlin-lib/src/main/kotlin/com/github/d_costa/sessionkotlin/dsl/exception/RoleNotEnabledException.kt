package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.SKRole

/**
 * Thrown when a role does not know the outcome of an external choice but its behaviour depends on it.
 */
public class RoleNotEnabledException(role: SKRole) : SessionKotlinDSLException("Role $role not enabled.")
