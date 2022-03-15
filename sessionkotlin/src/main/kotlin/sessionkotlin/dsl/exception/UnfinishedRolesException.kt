package sessionkotlin.dsl.exception

import sessionkotlin.dsl.Role

class UnfinishedRolesException(roles: Set<Role>):
    SessionKotlinException("Unfinished roles: ${roles.joinToString()}.")
