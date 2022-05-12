package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.SKRole

public class UnfinishedRolesException(vararg roles: SKRole) :
    SessionKotlinDSLException("Unfinished roles: ${roles.joinToString()}.")
