package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.SKRole

public class UnfinishedRolesException(vararg roles: SKRole) :
    SessionKotlinDSLException("Unfinished roles: ${roles.joinToString()}.")
