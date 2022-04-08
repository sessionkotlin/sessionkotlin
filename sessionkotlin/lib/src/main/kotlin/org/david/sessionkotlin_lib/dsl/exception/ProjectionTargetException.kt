package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.SKRole

public class ProjectionTargetException(role: SKRole) :
    SessionKotlinDSLException("Cannot project role $role, as it is not used in the protocol.")
