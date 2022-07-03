package com.github.d_costa.sessionkotlin.fsm

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.types.MsgLabel

internal typealias StateId = Int

public open class SimpleState(public open val id: StateId) {
    override fun toString(): String = "State[$id]"
}
internal object EndSimpleState : SimpleState(FSM.endStateIndex)

/**
 * Non-deterministic transition
 */
internal sealed class NDTransition(open val cont: Int)
internal data class TransitionWithAction(val action: Action, override val cont: Int) : NDTransition(cont)
internal data class Epsilon(override val cont: Int) : NDTransition(cont)

internal class SimpleTransition(val action: Action, val cont: Int)

internal sealed class Action(open val type: Class<*>, open val label: MsgLabel)
internal data class SendAction(val to: SKRole, override val type: Class<*>, override val label: MsgLabel, val condition: String) : Action(type, label)
internal data class ReceiveAction(val from: SKRole, override val type: Class<*>, override val label: MsgLabel) : Action(type, label)


internal sealed class Transition(val action: Action, val cont: Int)
internal class SendTransition(action: SendAction, cont: Int): Transition(action, cont)
internal class ReceiveTransition(action: ReceiveAction, cont: Int): Transition(action, cont)

internal sealed class State(open val id: StateId)
internal class ReceiveState(override val id: StateId, val transition: ReceiveTransition): State(id)
internal class SendState(override val id: StateId, val transition: SendTransition): State(id)
internal class InternalChoiceState(override val id: StateId, val transitions: List<SendTransition>): State(id)
internal class ExternalChoiceState(override val id: StateId, val from: SKRole, val transitions: List<ReceiveTransition>): State(id)
internal object EndState : State(FSM.endStateIndex)
