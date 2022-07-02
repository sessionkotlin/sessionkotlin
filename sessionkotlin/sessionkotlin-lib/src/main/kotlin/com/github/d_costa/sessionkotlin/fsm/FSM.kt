package com.github.d_costa.sessionkotlin.fsm

import com.github.d_costa.sessionkotlin.dsl.exception.NonDeterministicStatesException

internal typealias StateTransitions = Map<StateId, List<Transition>>

internal class FSM(val states: Set<State>, val transitions: StateTransitions) {
    companion object {
        const val initialStateIndex = 1
        const val endStateIndex = -1
    }

    init {
        for (s in states) {
            val ts = transitions.getOrElse(s.id, ::emptyList)
            val dupeLabels = ts
                .groupingBy { it.action.label.name }.eachCount().filter { it.value > 1 }

            if (dupeLabels.isNotEmpty()) {
                throw NonDeterministicStatesException(ts.filter { it.action.label.name in dupeLabels })
            }
        }
    }

    override fun toString(): String =
        StringBuilder()
            .appendLine("FSM:")
            .appendLine("states: {${states.joinToString()}}")
            .appendLine("transitions: {")
            .appendLine(
                transitions.map { (k, v) -> "> State $k: ${v.joinToString("\n", prefix = "\n")}" }
                    .joinToString("\n")
            )
            .appendLine("}")
            .toString()
}
