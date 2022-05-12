package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.SKRole

public class ProjectionTargetException(role: SKRole) :
    SessionKotlinDSLException("Cannot project role $role, as it is not used in the protocol.")
