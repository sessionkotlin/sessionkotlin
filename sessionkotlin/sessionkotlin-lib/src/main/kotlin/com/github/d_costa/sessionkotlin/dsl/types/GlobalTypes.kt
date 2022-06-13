package com.github.d_costa.sessionkotlin.dsl.types

import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.*
import com.github.d_costa.sessionkotlin.parser.RefinementParser

internal sealed interface GlobalType {
    /**
     * Projects the global type to a local view for [role].
     */
    fun project(role: SKRole, state: ProjectionState = ProjectionState(role)): LocalType

    /**
     * Checks refinement satisfiability.
     *
     * @return whether the refinements are satisfiable.
     */
    fun sat(state: SatState): Boolean
}

internal class GlobalTypeSend(
    private val from: SKRole,
    private val to: SKRole,
    private val type: Class<*>,
    private val msgLabel: String?,
    private val condition: String,
    private val cont: GlobalType,
) : GlobalType {
    override fun project(role: SKRole, state: ProjectionState): LocalType =
        when (role) {
            from -> {
                state.emptyRecursions.removeAll { true }
                if (!state.enabled()) {
                    state.sentWhileDisabled = true
                }
                if (msgLabel != null) {
                    state.names.add(msgLabel)
                }
                if (condition.isNotBlank()) {
                    val mentionedNames = RefinementParser.parseToEnd(condition).names()
                    val unknown = mentionedNames.minus(state.names)
                    if (unknown.isNotEmpty()) {
                        throw UnknownMessageLabelException(role, unknown)
                    }
                    state.usedNames.addAll(mentionedNames)
                }

                var branchLabel: String? = null
                if (!state.activeRoles.contains(to)) {
                    // We are activating role [to]
                    state.activeRoles.add(to)
                    branchLabel = state.branchLabel
                }
                val projectedCont = cont.project(role, state)

                val l: MsgLabel? = if (msgLabel != null)
                    MsgLabel(msgLabel, state.usedNames.contains(msgLabel))
                else null

                LocalTypeSend(
                    to, type, projectedCont,
                    branchLabel = branchLabel, msgLabel = l, condition = condition
                )
            }
            to -> {
                state.emptyRecursions.removeAll { true }

                if (!state.enabled()) {
                    state.enabledBy = from
                }
                if (msgLabel != null) {
                    state.names.add(msgLabel)
                }

                if (condition.isNotBlank()) {
                    val mentionedNames = RefinementParser.parseToEnd(condition).names()
                    state.usedNames.addAll(mentionedNames)
                }

                // Only the sender must enforce the condition

                val projectedCont = cont.project(role, state)

                val l: MsgLabel? = if (msgLabel != null)
                    MsgLabel(msgLabel, state.usedNames.contains(msgLabel))
                else null

                LocalTypeReceive(from, type, projectedCont, msgLabel = l)
            }
            else -> {
                state.activeRoles.add(to)
                cont.project(role, state)
            }
        }

    override fun sat(state: SatState): Boolean {
        if (msgLabel != null) {
            try {
                state.addVariable(msgLabel, type)
            } catch (e: InvalidRefinementValueException) {
                state.addUnsupportedVariableType(msgLabel, type)
            }
        }
        if (condition.isNotBlank()) {
            state.addCondition(condition)
        }
        return cont.sat(state)
    }
}

internal class GlobalTypeChoice(
    private val at: SKRole,
    private val branches: Map<String, GlobalType>,
) : GlobalType {
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
                val newState = state.copy(
                    role,
                    emptyRecursions = state.emptyRecursions
                )

                // Generate a new state for each branch, with the choice subject activated
                val states = branches.mapValues { newState.copy(branchLabel = it.key, activeRoles = mutableSetOf(at)) }
                var localType =
                    LocalTypeExternalChoice(at, branches.mapValues { it.value.project(role, states.getValue(it.key)) })

                localType = localType.removeRecursions(newState.emptyRecursions)

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

    override fun sat(state: SatState): Boolean = branches.all { it.value.sat(state.clone()) }
}

internal object GlobalTypeEnd : GlobalType {
    override fun project(role: SKRole, state: ProjectionState) = LocalTypeEnd
    override fun sat(state: SatState): Boolean = state.satisfiable()
}

internal class GlobalTypeRecursionDefinition(
    private val tag: RecursionTag,
    private val cont: GlobalType,
) : GlobalType {
    override fun project(role: SKRole, state: ProjectionState): LocalType {
        state.emptyRecursions.add(tag)
        val projectedContinuation = cont.project(role, state)
        return if (!projectedContinuation.containsTag(tag)) {
            // Recursion tag is not used. Skip recursion definition.
            projectedContinuation
        } else {
            LocalTypeRecursionDefinition(tag, projectedContinuation)
        }
    }

    override fun sat(state: SatState) = cont.sat(state)
}

internal class GlobalTypeRecursion(
    private val tag: RecursionTag,
) : GlobalType {
    override fun project(role: SKRole, state: ProjectionState): LocalType {
        if (!state.enabled()) {
            state.sentWhileDisabled = true
        }
        return LocalTypeRecursion(tag)
    }

    override fun sat(state: SatState) = state.satisfiable()
}