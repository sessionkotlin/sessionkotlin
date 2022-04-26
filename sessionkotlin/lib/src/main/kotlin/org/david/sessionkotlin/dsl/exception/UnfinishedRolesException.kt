package org.david.sessionkotlin.dsl.exception

import org.david.sessionkotlin.dsl.SKRole

public class UnfinishedRolesException(vararg roles: SKRole) :
    SessionKotlinDSLException("Unfinished roles: ${roles.joinToString()}.")
