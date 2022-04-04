package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.Role

public class ProjectionTargetException(role: Role) :
    SessionKotlinException("Cannot project role $role, as it is not used in the protocol.")
