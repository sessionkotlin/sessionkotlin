package org.david.sessionkotlin_lib.dsl.types

import org.david.sessionkotlin_lib.dsl.RecursionTag
import org.david.sessionkotlin_lib.dsl.SKRole
import org.david.sessionkotlin_lib.dsl.exception.InconsistentExternalChoiceException
import org.david.sessionkotlin_lib.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin_lib.dsl.exception.UnfinishedRolesException

internal abstract class GlobalType {
    internal abstract fun project(role: SKRole, state: State = State(role)): LocalType
}

internal data class State(
    val role: SKRole,
    var sentWhileDisabled: Boolean = false,
    var enabledBy: SKRole? = null,
) {
    fun enabled() = enabledBy != null && enabledBy != role
}

internal class GlobalTypeSend(
    private val from: SKRole,
    private val to: SKRole,
    private val type: Class<*>,
    private val cont: GlobalType,
) : GlobalType() {
    override fun project(role: SKRole, state: State): LocalType =
        when (role) {
            from -> {
                if (!state.enabled()) {
                    state.sentWhileDisabled = true
                }
                LocalTypeSend(to, type, cont.project(role, state))
            }
            to -> {
                if (!state.enabled()) {
                    state.enabledBy = from
                }
                LocalTypeReceive(from, type, cont.project(role, state))
            }
            else -> cont.project(role, state)
        }
}

internal class GlobalTypeBranch(
    private val at: SKRole,
    private val cases: Map<String, GlobalType>,
) : GlobalType() {
    override fun project(role: SKRole, state: State): LocalType {
        when (role) {
            at -> {
                if (!state.enabled()) {
                    state.sentWhileDisabled = true
                    state.enabledBy = at
                }
                val states = cases.mapValues { state.copy() }
                return LocalTypeInternalChoice(cases.mapValues { it.value.project(role, states.getValue(it.key)) })
            }
            else -> {
                val newState = State(role)

                val states = cases.mapValues { newState.copy() }
                val localType =
                    LocalTypeExternalChoice(at, cases.mapValues { it.value.project(role, states.getValue(it.key)) })

                val enabledBy: List<SKRole> = states.values
                    .mapNotNull { it.enabledBy }
                    .filter { it != state.role }

                // Check if the role was enabled by different roles in different cases
                if (enabledBy.toSet().size > 1) {
                    throw InconsistentExternalChoiceException(role, enabledBy)
                } else if (enabledBy.isNotEmpty()) {
                    localType.to = enabledBy.first()

                    if (state.enabledBy == null) {
                        state.enabledBy = enabledBy.first()
                    }
                }

                val c = states.values.count { it.enabled() }
                // Check if role is enabled in some but not all cases
                if (c != 0 && c != cases.size) {
                    throw UnfinishedRolesException(role)
                }

                // Erase empty choice
                if (localType.cases.values.all { it is LocalTypeEnd }) {
                    return LocalTypeEnd
                }

                // Check if the role sent a message without knowing the outcome of the decision
                return if (states.any { it.value.sentWhileDisabled }) {
                    // Then the projection must be the same for every case

                    val uniqueProjectedCases = localType.cases.values.toSet()

                    if (uniqueProjectedCases.size > 1) {
                        // The role's behaviour is not the same in all cases.
                        throw RoleNotEnabledException(role)
                    } else {
                        uniqueProjectedCases.first()
                    }
                } else {
                    localType
                }
            }
        }
    }
}

internal object GlobalTypeEnd : GlobalType() {
    override fun project(role: SKRole, state: State) = LocalTypeEnd
}

internal class GlobalTypeRecursionDefinition(
    private val tag: RecursionTag,
    private val cont: GlobalType,
) : GlobalType() {
    override fun project(role: SKRole, state: State): LocalType {
        val projectedContinuation = cont.project(role, state)
        return if (projectedContinuation is LocalTypeRecursion) {
            // Empty loop
            LocalTypeEnd
        } else if (!projectedContinuation.containsTag(tag)) {
            projectedContinuation
        } else {
            LocalTypeRecursionDefinition(tag, projectedContinuation)
        }
    }
}

internal class GlobalTypeRecursion(
    private val tag: RecursionTag,
) : GlobalType() {
    override fun project(role: SKRole, state: State): LocalType {
        if (!state.enabled()) {
            state.sentWhileDisabled = true
        }
        return LocalTypeRecursion(tag)
    }
}
