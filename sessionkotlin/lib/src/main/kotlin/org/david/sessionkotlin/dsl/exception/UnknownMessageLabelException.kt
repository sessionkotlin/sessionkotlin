package org.david.sessionkotlin.dsl.exception

import org.david.sessionkotlin.dsl.SKRole

public class UnknownMessageLabelException(role: SKRole, msgLabels: Set<String>) :
    SessionKotlinDSLException("Role $role cannot see some message labels: $msgLabels")
