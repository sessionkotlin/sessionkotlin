package com.github.d_costa.sessionkotlin.fsm

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.NonDeterministicStatesException

internal typealias StateTransitions = Map<StateId, List<SimpleTransition>>

internal class FSM(val states: Set<SimpleState>, val transitions: StateTransitions) {
    companion object {
        const val initialStateIndex = 1
        const val endStateIndex = -1
    }

    init {
        for (s in states) {
            val ts = transitions.getOrElse(s.id, ::emptyList)
            val dupeLabels = ts
                .groupingBy { it.action.label.name }.eachCount().filter { it.value > 1 }

            if (dupeLabels.isNotEmpty() && ts.any {it.action is ReceiveAction}) {
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

    fun asStates(): List<State> =
        states.map { s ->
            if (s is EndSimpleState) return@map EndState

            val ts = transitions.getValue(s.id)
            if (ts.all { it.action is SendAction }) {
                if (ts.size == 1) {
                    val t = ts.first()
                    SendState(s.id, SendTransition(t.action as SendAction, t.cont))
                } else {
                    InternalChoiceState(s.id, ts.map { SendTransition(it.action as SendAction, it.cont)})
                }
            } else if (ts.all { it.action is ReceiveAction }) {
                if (ts.size == 1) {
                    val t = ts.first()
                    ReceiveState(s.id, ReceiveTransition(t.action as ReceiveAction, t.cont))
                } else {
                    ExternalChoiceState(s.id, commonSource(ts), ts.map { ReceiveTransition(it.action as ReceiveAction, it.cont)})
                }
            } else throw RuntimeException("This should not have happened")
        }

    private fun commonSource(transitions: List<SimpleTransition>): SKRole {
        val sources = transitions.map { (it.action as ReceiveAction).from }.toSet()
        if (sources.size > 1)
            throw RuntimeException("Inconsistent external choice: [${sources.joinToString()}]")
        return sources.first()
    }
}
