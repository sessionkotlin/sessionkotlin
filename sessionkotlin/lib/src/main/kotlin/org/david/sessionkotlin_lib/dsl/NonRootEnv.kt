package org.david.sessionkotlin_lib.dsl

internal class NonRootEnv(
    roles: Set<Role>,
    recursionVariables: Set<RecursionTag>,
) : GlobalEnv(roles, recursionVariables)
