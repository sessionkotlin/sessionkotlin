package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.Role

public class UnfinishedRolesException(vararg roles: Role) :
    SessionKotlinException("Unfinished roles: ${roles.joinToString()}.")
