package com.github.sessionkotlin.lib.dsl.exception

import com.github.sessionkotlin.lib.dsl.SKRole

/**
 * Thrown when a role does not know the outcome of an external choice but its behaviour depends on it.
 */
internal class RoleNotEnabledException(role: SKRole) : SessionKotlinDSLException("Role $role not enabled.")
