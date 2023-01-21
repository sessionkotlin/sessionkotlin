package com.github.sessionkotlin.lib.dsl.types

import com.github.sessionkotlin.lib.dsl.RecursionTag
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.exception.*
import com.github.sessionkotlin.parser.RefinementCondition
import org.sosy_lab.java_smt.api.BooleanFormula

internal sealed interface GlobalType {
    /**
     * Projects the global type to a local view for [role].
     */
    fun project(role: SKRole, state: ProjectionState = ProjectionState(role)): LocalType

    /**
     * Checks refinement satisfiability.
     *
     * @return a list of unsatisfiable formulas.
     *
     * Since it is more probable that a system is satisfiable,
     * the solver should only be called at a terminal state,
     * instead of verifying everytime a new condition is added.
     */
    fun getUnsat(state: SatState): List<BooleanFormula>
}

internal class GlobalTypeSend(
    private val from: SKRole,
    private val to: SKRole,
    private val type: Class<*>,
    private val msgLabel: String,
    private val condition: RefinementCondition?,
    private val cont: GlobalType,
) : GlobalType {
    override fun project(role: SKRole, state: ProjectionState): LocalType =
        when (role) {
            from -> {
                state.emptyRecursions.removeAll { true }
                if (!state.enabled()) {
                    state.sentWhileDisabled = true
                }
                state.names.add(msgLabel)

                if (condition != null) {
                    val mentionedNames = condition.expression.names()
                    val unknown = mentionedNames.minus(state.names)
                    if (unknown.isNotEmpty()) {
                        throw UnknownMessageLabelException(role, unknown)
                    }
                    state.usedNames.addAll(mentionedNames)
                }

                if (!state.activeRoles.contains(to)) {
                    // We are activating role [to]
                    state.activeRoles.add(to)
                }
                val projectedCont = cont.project(role, state)

                val l = MsgLabel(msgLabel, state.usedNames.contains(msgLabel))

                LocalTypeSend(to, type, l, condition, projectedCont)
            }
            to -> {
                state.emptyRecursions.removeAll { true }

                if (!state.enabled()) {
                    state.enabledBy = from
                }
                state.names.add(msgLabel)

                if (condition != null) {
                    state.usedNames.addAll(condition.expression.names())
                }

                // Only the sender must enforce the condition

                val projectedCont = cont.project(role, state)

                val l = MsgLabel(msgLabel, state.usedNames.contains(msgLabel))

                LocalTypeReceive(from, type, msgLabel = l, projectedCont)
            }
            else -> {
                state.activeRoles.add(to)
                cont.project(role, state)
            }
        }

    override fun getUnsat(state: SatState): List<BooleanFormula> {
        try {
            state.addVariable(msgLabel, type)
        } catch (e: InvalidRefinementValueException) {
            state.addUnsupportedVariableType(msgLabel, type)
        }
        if (condition != null) {
            state.addCondition(condition.expression)
        }
        return cont.getUnsat(state)
    }
}

internal class GlobalTypeChoice(
    private val at: SKRole,
    private val branches: List<GlobalType>,
) : GlobalType {
    override fun project(role: SKRole, state: ProjectionState): LocalType {
        when (role) {
            at -> {
                if (!state.enabled()) {
                    state.sentWhileDisabled = true
                    state.enabledBy = at
                }

                val states = branches.map { state.copy(activeRoles = mutableSetOf(at)) }
                val mappedBranches = branches.mapIndexed { i, g -> g.project(role, states[i]) }
                val uniqueProjectedBranches = mappedBranches.toSet()

                return LocalTypeInternalChoice(uniqueProjectedBranches)
            }
            else -> {
                val newState = state.copy(
                    role,
                    emptyRecursions = state.emptyRecursions
                )

                // Generate a new state for each branch, with the choice subject activated
                val states = branches.map { newState.copy(activeRoles = mutableSetOf(at)) }
                var localType = LocalTypeExternalChoice(at, branches.mapIndexed { i, g -> g.project(role, states[i]) })

                localType = localType.removeRecursions(newState.emptyRecursions) // TODO really needed?

                /**
                 * The roles that enabled the projected role
                 */
                val enabledBy: List<SKRole> = states
                    .mapNotNull { it.enabledBy }
                    .filter { it != role }

                if (enabledBy.toSet().size > 1) {
                    // The role was enabled by different roles in different branches
                    throw InconsistentExternalChoiceException(role, enabledBy)
                } else if (enabledBy.isNotEmpty()) {
                    // The role was enabled by the same role in all branches
                    localType.of = enabledBy.first()

                    if (state.enabledBy == null) {
                        // role is active
                        state.enabledBy = enabledBy.first()
                    }
                }

                val count = states.count { it.enabled() }
                if (count != 0 && count != branches.size) {
                    // Role is enabled in some but not all branches
                    throw UnfinishedRolesException(role)
                }

                // Erase empty choice
                if (localType.branches.all { it is LocalTypeEnd }) {
                    return LocalTypeEnd
                }

                // Prune duplicate branches
                val uniqueProjectedBranches = localType.branches.toSet()

                if (states.any { it.sentWhileDisabled } && uniqueProjectedBranches.size > 1) {
                    // Role sent a message without knowing the outcome of the decision
                    // The projection must be the same for all branches
                    throw RoleNotEnabledException(role)
                }

                return if (uniqueProjectedBranches.size == 1)
                    uniqueProjectedBranches.first()
                else
                    LocalTypeExternalChoice(localType.of, uniqueProjectedBranches)
            }
        }
    }

    override fun getUnsat(state: SatState): List<BooleanFormula> {
        for (b in branches) {
            val l = b.getUnsat(state.clone())
            if (l.isNotEmpty()) {
                // no need to check the other branches
                return l
            }
        }
        return emptyList()
    }
}

internal object GlobalTypeEnd : GlobalType {
    override fun project(role: SKRole, state: ProjectionState) = LocalTypeEnd
    override fun getUnsat(state: SatState): List<BooleanFormula> = state.satisfiable()
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

    override fun getUnsat(state: SatState): List<BooleanFormula> = cont.getUnsat(state)
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

    override fun getUnsat(state: SatState): List<BooleanFormula> = state.satisfiable()
}
