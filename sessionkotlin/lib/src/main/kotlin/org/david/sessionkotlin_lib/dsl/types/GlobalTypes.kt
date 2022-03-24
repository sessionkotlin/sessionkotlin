package org.david.sessionkotlin_lib.dsl.types

import org.david.sessionkotlin_lib.dsl.Role

internal abstract class GlobalType {
    abstract fun project(role: Role): LocalType
}

internal class GlobalTypeSend(
    private val from: Role,
    private val to: Role,
    private val type: Class<*>,
    private val cont: GlobalType,
) : GlobalType() {
    override fun project(role: Role): LocalType =
        when (role) {
            from -> LocalTypeSend(to, type, cont.project(role))
            to -> LocalTypeReceive(from, type, cont.project(role))
            else -> LocalTypeNOP
        }
}

internal class GlobalTypeBranch(
    private val at: Role,
    private val cases: Map<String, GlobalType>,
) : GlobalType() {
    override fun project(role: Role): LocalType =
        when (role) {
            at -> LocalTypeInternalChoice(cases.mapValues { it.value.project(role) })
            else -> LocalTypeExternalChoice(at, cases.mapValues { it.value.project(role) }) // TODO validation
        }
}

internal object GlobalTypeEnd : GlobalType() {
    override fun project(role: Role) = LocalTypeEnd
}

internal object GlobalTypeRec : GlobalType() {
    override fun project(role: Role) = LocalTypeRec
}
