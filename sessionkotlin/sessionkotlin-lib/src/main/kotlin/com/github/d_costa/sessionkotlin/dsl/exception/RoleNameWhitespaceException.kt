package com.github.d_costa.sessionkotlin.dsl.exception

/**
 * Thrown when attempting to use an invalid role name.
 */
internal class RoleNameWhitespaceException(label: String) :
    SessionKotlinDSLException("Role name '$label' is invalid. Cannot contain whitespace.")
