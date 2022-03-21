package dsl.exception

import dsl.Role

class UnfinishedRolesException(roles: Set<Role>) :
    SessionKotlinException("Unfinished roles: ${roles.joinToString()}.")
