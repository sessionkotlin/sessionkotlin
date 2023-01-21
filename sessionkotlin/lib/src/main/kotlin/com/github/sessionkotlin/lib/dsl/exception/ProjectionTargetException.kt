package com.github.sessionkotlin.lib.dsl.exception

import com.github.sessionkotlin.lib.dsl.SKRole

/**
 * Thrown when attempting to project a global type for a role that does not participate in it.
 */
internal class ProjectionTargetException(role: SKRole) :
    SessionKotlinDSLException("Cannot project role $role, as it is not used in the protocol.")
