package com.github.d_costa.sessionkotlin.dsl.types

import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.InconsistentExternalChoiceException
import com.github.d_costa.sessionkotlin.dsl.exception.RoleNotEnabledException
import com.github.d_costa.sessionkotlin.dsl.exception.UnfinishedRolesException
import com.github.d_costa.sessionkotlin.dsl.exception.UnknownMessageLabelException
import com.github.d_costa.sessionkotlin.parser.RefinementParser

internal abstract class GlobalType {
    internal abstract fun project(role: SKRole, state: ProjectionState = ProjectionState(role)): LocalType
    internal abstract fun visitRefinements(state: SatState)
}

internal class GlobalTypeSend(
    private val from: SKRole,
    private val to: SKRole,
    private val type: Class<*>,
    private val msgLabel: String?,
    private val condition: String,
    private val cont: GlobalType,
) : GlobalType() {
    override fun project(role: SKRole, state: ProjectionState): LocalType =
        when (role) {
            from -> {
                state.unguardedRecursions.removeAll { true }
                if (!state.enabled()) {
                    state.sentWhileDisabled = true
                }
                if (msgLabel != null) {
                    state.names.add(msgLabel)
                }
                if (condition.isNotBlank()) {
                    val unknown = RefinementParser.parseToEnd(condition).names().minus(state.names)
                    if (unknown.isNotEmpty()) {
                        throw UnknownMessageLabelException(role, unknown)
                    }
                }

                if (!state.activeRoles.contains(to)) {
                    // We are activating role [to]
                    state.activeRoles.add(to)
                    LocalTypeSend(
                        to, type, cont.project(role, state),
                        branchLabel = state.branchLabel, msgLabel = msgLabel, condition = condition
                    )
                } else {
                    LocalTypeSend(
                        to, type, cont.project(role, state),
                        msgLabel = msgLabel, condition = condition
                    )
                }
            }
            to -> {
                state.unguardedRecursions.removeAll { true }

                if (!state.enabled()) {
                    state.enabledBy = from
                }
                if (msgLabel != null) {
                    state.names.add(msgLabel)
                }
                // Only the sender must enforce the condition

                LocalTypeReceive(from, type, cont.project(role, state), msgLabel = msgLabel)
            }
            else -> {
                state.activeRoles.add(to)
                cont.project(role, state)
            }
        }

    override fun visitRefinements(state: SatState) {
        if (msgLabel != null) {
            state.addVariable(msgLabel)
        }
        if (condition.isNotBlank()) {
            state.addCondition(condition)
        }
    }
}

internal class GlobalTypeChoice(
    private val at: SKRole,
    private val branches: Map<String, GlobalType>,
) : GlobalType() {
    override fun project(role: SKRole, state: ProjectionState): LocalType {
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
                val newState = state.copy(role, names = state.names.toMutableSet(), unguardedRecursions = state.unguardedRecursions)

                // Generate a new state for each branch, with the choice subject activated
                val states = branches.mapValues { newState.copy(branchLabel = it.key, activeRoles = mutableSetOf(at)) }
                var localType =
                    LocalTypeExternalChoice(at, branches.mapValues { it.value.project(role, states.getValue(it.key)) })

                localType = localType.removeRecursions(newState.unguardedRecursions)

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

    override fun visitRefinements(state: SatState) = branches.forEach { it.value.visitRefinements(state) }
}

internal object GlobalTypeEnd : GlobalType() {
    override fun project(role: SKRole, state: ProjectionState) = LocalTypeEnd
    override fun visitRefinements(state: SatState) = Unit
}

internal class GlobalTypeRecursionDefinition(
    private val tag: RecursionTag,
    private val cont: GlobalType,
) : GlobalType() {
    override fun project(role: SKRole, state: ProjectionState): LocalType {
        state.unguardedRecursions.add(tag)
        val projectedContinuation = cont.project(role, state)
        return if (!projectedContinuation.containsTag(tag)) {
            // Recursion tag is not used. Skip recursion definition.
            projectedContinuation
        } else {
            LocalTypeRecursionDefinition(tag, projectedContinuation)
        }
    }
    override fun visitRefinements(state: SatState) = cont.visitRefinements(state)
}

internal class GlobalTypeRecursion(
    private val tag: RecursionTag,
) : GlobalType() {
    override fun project(role: SKRole, state: ProjectionState): LocalType {
        if (!state.enabled()) {
            state.sentWhileDisabled = true
        }
        return LocalTypeRecursion(tag)
    }

    override fun visitRefinements(state: SatState) = Unit
}
