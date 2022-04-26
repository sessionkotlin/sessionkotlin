package org.david.sessionkotlin.dsl.exception

import org.david.sessionkotlin.dsl.SKRole

public class ProjectionTargetException(role: SKRole) :
    SessionKotlinDSLException("Cannot project role $role, as it is not used in the protocol.")
