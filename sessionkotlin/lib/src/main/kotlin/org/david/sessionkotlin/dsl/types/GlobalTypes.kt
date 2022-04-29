package org.david.sessionkotlin.dsl.types

import org.david.sessionkotlin.dsl.RecursionTag
import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.exception.InconsistentExternalChoiceException
import org.david.sessionkotlin.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin.dsl.exception.UnfinishedRolesException

internal abstract class GlobalType {
    internal abstract fun project(role: SKRole, state: State = State(role)): LocalType
}

internal data class State(
    val projectedRole: SKRole,

    /**
     * Whether the projected role sent a message, made choice or had recursion while not knowing
     * the outcome of a choice
     */
    var sentWhileDisabled: Boolean = false,

    /**
     * The role that enabled the projected role (i.e. the sender of the first message received
     * in a branch)
     */
    var enabledBy: SKRole? = null,

    /**
     * The roles that received a message in the branch
     */
    var activeRoles: MutableSet<SKRole> = mutableSetOf(),

    /**
     * Label for the current branch
     */
    var branchLabel: String? = null,
) {
    /**
     * Returns whether the [projectedRole] is enabled.
     */
    fun enabled() = enabledBy != null && enabledBy != projectedRole
}

internal class GlobalTypeSend(
    private val from: SKRole,
    private val to: SKRole,
    private val type: Class<*>,
    private val msgLabel: String?,
    private val cont: GlobalType,
) : GlobalType() {
    override fun project(role: SKRole, state: State): LocalType =
        when (role) {
            from -> {
                if (!state.enabled()) {
                    state.sentWhileDisabled = true
                }
                if (!state.activeRoles.contains(to)) {
                    // We are activating role [to]
                    state.activeRoles.add(to)
                    LocalTypeSend(
                        to, type, cont.project(role, state),
                        branchLabel = state.branchLabel, msgLabel = msgLabel
                    )
                } else {
                    LocalTypeSend(
                        to, type, cont.project(role, state),
                        msgLabel = msgLabel
                    )
                }
            }
            to -> {
                if (!state.enabled()) {
                    state.enabledBy = from
                }
                LocalTypeReceive(from, type, cont.project(role, state), msgLabel = msgLabel)
            }
            else -> {
                state.activeRoles.add(to)
                cont.project(role, state)
            }
        }
}

internal class GlobalTypeChoice(
    private val at: SKRole,
    private val branches: Map<String, GlobalType>,
) : GlobalType() {
    override fun project(role: SKRole, state: State): LocalType {
        when (role) {
            at -> {
                if (!state.enabled()) {
                    state.sentWhileDisabled = true
                    state.enabledBy = at
                }
                val states = branches.mapValues { state.copy(branchLabel = it.key, activeRoles = mutableSetOf(at)) }
                return LocalTypeInternalChoice(branches.mapValues { it.value.project(role, states.getValue(it.key)) })
            }
            else -> {
                val newState = State(role)

                // Generate a new state for each branch, with the choice subject activated
                val states = branches.mapValues { newState.copy(branchLabel = it.key, activeRoles = mutableSetOf(at)) }
                val localType =
                    LocalTypeExternalChoice(at, branches.mapValues { it.value.project(role, states.getValue(it.key)) })

                /**
                 * The roles that enabled the projected role
                 */
                val enabledBy: List<SKRole> = states.values
                    .mapNotNull { it.enabledBy }
                    .filter { it != role }

                if (enabledBy.toSet().size > 1) {
                    // The role was enabled by different roles in different branches
                    throw InconsistentExternalChoiceException(role, enabledBy)
                } else if (enabledBy.isNotEmpty()) {
                    // The role was enabled by the same role in all branches
                    localType.to = enabledBy.first()

                    if (state.enabledBy == null) {
                        // role is active
                        state.enabledBy = enabledBy.first()
                    }
                }

                val count = states.values.count { it.enabled() }
                if (count != 0 && count != branches.size) {
                    // Role is enabled in some but not all branches
                    throw UnfinishedRolesException(role)
                }

                // Erase empty choice
                if (localType.branches.values.all { it is LocalTypeEnd }) {
                    return LocalTypeEnd
                }

                return if (states.any { it.value.sentWhileDisabled }) {
                    // Role sent a message without knowing the outcome of the decision
                    // The projection must be the same for all branches

                    val uniqueProjectedBranches = localType.branches.values.toSet()

                    if (uniqueProjectedBranches.size > 1) {
                        // The role's behaviour is not the same in all branches.
                        throw RoleNotEnabledException(role)
                    } else {
                        uniqueProjectedBranches.first()
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
            // Recursion tag is not used. Skip recursion definition.
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
