package com.github.sessionkotlin.lib.dsl.exception

import com.github.sessionkotlin.lib.dsl.SKRole

/**
 * Thrown when there are roles in an unfinished state and the protocol is finished.
 */
internal class UnfinishedRolesException(vararg roles: SKRole) :
    SessionKotlinDSLException("Unfinished roles: ${roles.joinToString()}.")
