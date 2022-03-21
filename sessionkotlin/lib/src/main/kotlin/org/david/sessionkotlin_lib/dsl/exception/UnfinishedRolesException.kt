package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.Role

class UnfinishedRolesException(roles: Set<Role>) :
    SessionKotlinException("Unfinished roles: ${roles.joinToString()}.")
