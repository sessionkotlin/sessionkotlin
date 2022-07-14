package com.github.d_costa.sessionkotlin.fsm

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.types.MsgLabel

internal typealias StateId = Int
internal typealias StateTransitions = Map<StateId, List<SimpleTransition>>

internal open class SimpleState(open val id: StateId) {
    override fun toString(): String = "State[$id]"
}
internal object EndSimpleState : SimpleState(State.endStateIndex)

/**
 * Non-deterministic transition
 */
internal sealed class NDTransition(open val cont: Int)
internal data class TransitionWithAction(val action: Action, override val cont: Int) : NDTransition(cont)
internal data class Epsilon(override val cont: Int) : NDTransition(cont)

internal data class SimpleTransition(val action: Action, val cont: Int)

internal sealed class Action(open val type: Class<*>, open val label: MsgLabel)
internal data class SendAction(val to: SKRole, override val type: Class<*>, override val label: MsgLabel, val condition: String) : Action(type, label)
internal data class ReceiveAction(val from: SKRole, override val type: Class<*>, override val label: MsgLabel) : Action(type, label)

internal sealed class Transition(open val action: Action, open val cont: Int)
internal data class SendTransition(override val action: SendAction, override val cont: Int) : Transition(action, cont)
internal data class ReceiveTransition(override val action: ReceiveAction, override val cont: Int) : Transition(action, cont)

internal sealed class State(open val id: StateId) {
    companion object {
        const val initialStateIndex = 1
        const val endStateIndex = -1
    }
}
internal data class ReceiveState(override val id: StateId, val transition: ReceiveTransition) : State(id)
internal data class SendState(override val id: StateId, val transition: SendTransition) : State(id)
internal data class InternalChoiceState(override val id: StateId, val transitions: List<Transition>) : State(id)
internal data class ExternalChoiceState(override val id: StateId, val from: SKRole, val transitions: List<ReceiveTransition>) : State(id)
internal object EndState : State(endStateIndex) {
    override fun toString() = "${this::class.simpleName ?: super.toString()}($id)"
}
