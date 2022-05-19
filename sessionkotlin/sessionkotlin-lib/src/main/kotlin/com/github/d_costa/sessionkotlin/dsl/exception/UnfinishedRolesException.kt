package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.SKRole

/**
 * Thrown when there are roles in an unfinished state and the protocol is finished.
 */
public class UnfinishedRolesException(vararg roles: SKRole) :
    SessionKotlinDSLException("Unfinished roles: ${roles.joinToString()}.")
