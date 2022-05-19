package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.SKRole

/**
 * Thrown when attempting to project a global type for a role that does not participate in it.
 */
public class ProjectionTargetException(role: SKRole) :
    SessionKotlinDSLException("Cannot project role $role, as it is not used in the protocol.")
