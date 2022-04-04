package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.Role

public class UnfinishedRolesException(vararg roles: Role) :
    SessionKotlinDSLException("Unfinished roles: ${roles.joinToString()}.")
