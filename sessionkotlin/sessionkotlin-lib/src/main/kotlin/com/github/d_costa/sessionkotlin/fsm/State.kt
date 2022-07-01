package com.github.d_costa.sessionkotlin.fsm

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.types.MsgLabel

internal typealias StateId = Int

internal open class State(open val id: StateId) {
    override fun toString(): String = "State[$id]"
}
internal object EndState : State(FSM.endStateIndex)

/**
 * Non-deterministic transition
 */
internal sealed class NDTransition(open val cont: Int)
internal data class TransitionWithAction(val action: Action, override val cont: Int) : NDTransition(cont)
internal data class Epsilon(override val cont: Int) : NDTransition(cont)

internal data class Transition(val action: Action, val cont: Int)

internal sealed interface Action
internal data class SendAction(val to: SKRole, val type: Class<*>, val label: MsgLabel, val condition: String) : Action
internal data class ReceiveAction(val from: SKRole, val type: Class<*>, val label: MsgLabel) : Action
