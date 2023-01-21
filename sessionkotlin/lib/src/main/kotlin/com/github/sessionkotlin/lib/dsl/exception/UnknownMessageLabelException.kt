package com.github.sessionkotlin.lib.dsl.exception

import com.github.sessionkotlin.lib.dsl.SKRole

/**
 * Thrown when the role does not have access to some messages referenced in a refinement expression.
 */
internal class UnknownMessageLabelException(role: SKRole, msgLabels: Set<String>) :
    SessionKotlinDSLException("Role $role cannot see some message labels: $msgLabels")
