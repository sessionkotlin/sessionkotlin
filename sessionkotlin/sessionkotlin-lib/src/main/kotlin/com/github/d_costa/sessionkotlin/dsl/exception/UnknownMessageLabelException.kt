package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.SKRole

/**
 * Thrown when the role does not have access to some messages referenced in a refinement expression.
 */
public class UnknownMessageLabelException(role: SKRole, msgLabels: Set<String>) :
    SessionKotlinDSLException("Role $role cannot see some message labels: $msgLabels")
