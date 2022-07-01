package com.github.d_costa.sessionkotlin.fsm

internal typealias StateTransitions = Map<StateId, List<Transition>>

internal class FSM(val states: Set<State>, val transitions: StateTransitions) {
    companion object {
        const val initialStateIndex = 1
        const val endStateIndex = -1
    }

    override fun toString(): String =
        StringBuilder()
            .appendLine("FSM:")
            .appendLine("states: {${states.joinToString()}}")
            .appendLine("transitions: {")
            .appendLine(transitions.map { (k, v) -> "> State $k: ${v.joinToString("\n", prefix = "\n")}" }.joinToString("\n"))
            .appendLine("}")
            .toString()
}
