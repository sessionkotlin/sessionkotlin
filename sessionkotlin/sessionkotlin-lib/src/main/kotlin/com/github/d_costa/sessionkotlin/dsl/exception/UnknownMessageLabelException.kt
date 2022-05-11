package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.SKRole

public class UnknownMessageLabelException(role: SKRole, msgLabels: Set<String>) :
    SessionKotlinDSLException("Role $role cannot see some message labels: $msgLabels")
