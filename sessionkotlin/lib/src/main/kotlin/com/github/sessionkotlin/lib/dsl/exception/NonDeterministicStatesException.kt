package com.github.sessionkotlin.lib.dsl.exception

import com.github.sessionkotlin.lib.fsm.ReceiveAction
import com.github.sessionkotlin.lib.fsm.SendAction
import com.github.sessionkotlin.lib.fsm.SimpleTransition

internal class NonDeterministicStatesException(labels: List<SimpleTransition>) :
    SessionKotlinDSLException("Non-deterministic state: ${str(labels)}")

private fun str(labels: List<SimpleTransition>) =
    labels.map {
        when (it.action) {
            is ReceiveAction -> "Receive ${it.action.label.name} from ${it.action.from}"
            is SendAction -> "Send ${it.action.label.name} to ${it.action.to}"
        }
    }
