package org.david.sessionkotlin_lib.dsl.types

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.InconsistentExternalChoiceException
import org.david.sessionkotlin_lib.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin_lib.dsl.exception.UnfinishedRolesException

internal abstract class GlobalType {
    abstract fun project(role: Role, state: State): LocalType
}

internal data class State(
    var enabled: Boolean = true,
    var sentWhileDisabled: Boolean = false,
    var enabledBy: Role? = null,
)


internal class GlobalTypeSend(
    private val from: Role,
    private val to: Role,
    private val type: Class<*>,
    private val cont: GlobalType,
) : GlobalType() {
    override fun project(role: Role, state: State): LocalType =
        when (role) {
            from -> {
                if (!state.enabled) {
                    state.sentWhileDisabled = true
                }
                LocalTypeSend(to, type, cont.project(role, state))
            }
            to -> {
                if (!state.enabled) {
                    state.enabled = true
                    state.enabledBy = from
                }
                LocalTypeReceive(from, type, cont.project(role, state))
            }
            else -> cont.project(role, state)
        }
}

internal class GlobalTypeBranch(
    private val at: Role,
    private val cases: Map<String, GlobalType>,
) : GlobalType() {
    override fun project(role: Role, state: State): LocalType =
        when (role) {
            at -> {
                if (!state.enabled) {
                    state.sentWhileDisabled = true
                    state.enabled = true
                }
                val states = cases.mapValues { state.copy() }
                LocalTypeInternalChoice(cases.mapValues { it.value.project(role, states.getValue(it.key)) })
            }
            else -> {
                val newState = State(enabled = false)

                val states = cases.mapValues { newState.copy() }
                val localType =
                    LocalTypeExternalChoice(at, cases.mapValues { it.value.project(role, states.getValue(it.key)) })

                val enabledBy: List<Role> = states.values.mapNotNull { it.enabledBy }

                // Check if the role was enabled by different roles in different cases
                if (states.values.mapNotNull { it.enabledBy }.toSet().size > 1) {
                    throw InconsistentExternalChoiceException(role, enabledBy)
                }

                val c = states.values.count { it.enabled }
                // Check if role is enabled in some but not all cases
                if (c != 0 && c != cases.size) {
                    throw UnfinishedRolesException(role)
                }

                // Role is enabled it is enabled in all cases
                state.enabled = states.values.all { it.enabled }

                // Check if the role sent a message without knowing the outcome of the decision
                if (states.any { it.value.sentWhileDisabled }) {
                    // Then the projection must be the same for every case

                    val projectedCases = localType.cases.values.toSet()
                    if (projectedCases.size > 1) {
                        // The role's behaviour is not the same in all cases.
                        throw RoleNotEnabledException(role)
                    } else if (projectedCases.isEmpty()) {
                        // This "role"s behaviour *is* the same in all cases.
                        LocalTypeEnd
                    } else {
                        projectedCases.first()
                    }
                } else {
                    localType
                }
            }


        }
}

internal object GlobalTypeEnd : GlobalType() {
    override fun project(role: Role, state: State) = LocalTypeEnd
}

internal object GlobalTypeRec : GlobalType() {
    override fun project(role: Role, state: State) = LocalTypeRec
}
