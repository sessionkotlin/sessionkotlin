package com.github.d_costa.sessionkotlin.fsm


/**
 * Non-deterministic FSM
 */
internal class NDFSM(val states: Set<State>, val transitions: Map<StateId, List<NDTransition>>) {
    override fun toString(): String =
        StringBuilder()
            .appendLine("FSM:")
            .appendLine("states: {${states.joinToString()}}")
            .appendLine("transitions: {")
            .appendLine(transitions.map { (k, v) -> "> State $k: ${v.joinToString("\n", prefix = "\n")}" }.joinToString("\n"))
            .appendLine("}")
            .toString()
}
