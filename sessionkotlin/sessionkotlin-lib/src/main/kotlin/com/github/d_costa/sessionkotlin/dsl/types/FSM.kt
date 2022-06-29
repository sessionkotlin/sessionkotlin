package com.github.d_costa.sessionkotlin.dsl.types

import com.github.d_costa.sessionkotlin.dsl.SKRole

internal sealed interface State
internal class InputState(actions: List<ReceiveTransition>): State
internal class OutputState(actions: List<SendTransition>): State
internal object EndState: State

internal class SendTransition(action: SendAction, nextState: State)
internal class ReceiveTransition(action: ReceiveAction, nextState: State)

internal sealed interface Action
internal class SendAction(to: SKRole, type: Class<*>, label: MsgLabel, condition: String): Action
internal class ReceiveAction(from: SKRole, type: Class<*>, label: MsgLabel): Action

internal class FSM() {
    companion object {
        internal fun fromLocalType(l: LocalType): State {

            when(l) {
                LocalTypeEnd -> EndState
                is LocalTypeExternalChoice -> TODO()
                is LocalTypeInternalChoice -> {
                    val transitions = mutableListOf<SendTransition>()
                    for (b in l.branches) {
                        val action = SendAction(b.to, l.type, l.msgLabel, l.condition)
                        transitions.add(SendTransition(action, fromLocalType(l.cont)))
                    }
                    OutputState(transitions)
                }
                is LocalTypeReceive -> {
                    val a = ReceiveAction(l.from, l.type, l.msgLabel)
                    val t = ReceiveTransition(a, fromLocalType(l.cont))
                    InputState(listOf(t))
                }
                is LocalTypeRecursion -> TODO()
                is LocalTypeRecursionDefinition -> TODO()
                is LocalTypeSend -> {
                    val action = SendAction(l.to, l.type, l.msgLabel, l.condition)
                    val transition = SendTransition(action, fromLocalType(l.cont))
                    OutputState(listOf(transition))
                }
            }
        }
    }

}