package com.github.d_costa.sessionkotlin.fsm

import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.types.LocalType
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeEnd
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeExternalChoice
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeInternalChoice
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeReceive
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeRecursion
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeRecursionDefinition
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeSend
import com.github.d_costa.sessionkotlin.util.*

internal typealias LocalTypeId = Int

internal fun fromLocalType(localType: LocalType): FSM {
    fun LocalType.id() = hashCode()

    /**
     * Map [RecursionTag]s to [LocalType] ids
     */
    val recursions = mutableMapOf<RecursionTag, LocalTypeId>()

    /**
     * Map [LocalType] ids to a [State] ids
     */
    val memo = mutableMapOf<LocalTypeId, State>(Pair(LocalTypeEnd.id(), EndState))

    val states = mutableSetOf<State>(EndState)
    val stateTransitions = mutableMapOf<StateId, MutableList<NDTransition>>()
    var index = FSM.initialStateIndex

    fun buildStates(l: LocalType) {
        if (l.id() in memo) {
            // Skip
            return
        }

        fun createState(): StateId {
            val s = State(index++)
            states.add(s)

            if (l.id() !in memo) {
                // Save for future references
                memo[l.id()] = s
            }

            return s.id
        }

        when (l) {
            is LocalTypeRecursionDefinition -> {
                val id = createState()
                stateTransitions.merge(id, Epsilon(l.cont.id()))

                recursions[l.tag] = l.id()
                buildStates(l.cont)
            }
            is LocalTypeRecursion -> {
                val id = createState()
                stateTransitions.merge(id, Epsilon(recursions.getValue(l.tag)))
            }

            is LocalTypeReceive -> {
                val id = createState()

                val action = ReceiveAction(l.from, l.type, l.msgLabel)
                val transition = TransitionWithAction(action, l.cont.id())
                stateTransitions.merge(id, transition)

                buildStates(l.cont)
            }
            is LocalTypeExternalChoice -> {
                val id = createState()

                val ts: MutableList<NDTransition> = l.branches.mapMutable { Epsilon(it.id()) }
                stateTransitions.merge(id, ts)

                l.branches.forEach { buildStates(it) }
            }

            is LocalTypeSend -> {
                val id = createState()

                val action = SendAction(l.to, l.type, l.msgLabel, l.condition)
                val transition = TransitionWithAction(action, l.cont.id())
                stateTransitions.merge(id, transition)

                buildStates(l.cont)
            }
            is LocalTypeInternalChoice -> {
                val id = createState()

                val transitions: MutableList<NDTransition> = l.branches.mapMutable { Epsilon(it.id()) }
                stateTransitions.merge(id, transitions)

                l.branches.forEach { buildStates(it) }
            }

            LocalTypeEnd -> {
                // Do nothing
            }
        }
    }

    /**
     * Replace LocalType references with NaiveState references
     */
    fun translateIds(transitions: Map<StateId, List<NDTransition>>, mapper: Map<LocalTypeId, State>) =
        transitions.mapValues { (_, v) ->
            v.map {
                when (it) {
                    is Epsilon -> Epsilon(mapper.getValue(it.cont).id)
                    is TransitionWithAction -> TransitionWithAction(it.action, mapper.getValue(it.cont).id)
                }
            }
        }

    buildStates(localType)
    val naiveFSM = NDFSM(states, translateIds(stateTransitions, memo))

    return simplify(naiveFSM)
}


/**
 * Create a simplified graph by removing epsilon transitions and unreachable states
 */
private fun simplify(fsm: NDFSM): FSM {
    val transitionsWithoutEpsilon = fsm.transitions.mapValues { (k, _) ->
        getTransitions(k, fsm)
    }

    val initialState = fsm.states.find { it.id == FSM.initialStateIndex } ?: throw RuntimeException("No initial state!")
    val reachableStateIds: Set<StateId> = reachable(setOf(initialState.id), transitionsWithoutEpsilon)

    val states = fsm.states.filter { reachableStateIds.contains(it.id) }.toSet()
    val transitions = transitionsWithoutEpsilon.filterKeys { reachableStateIds.contains(it) }
    return FSM(states, transitions)
}

/**
 * Calculates the transitions available to a State, following epsilon transitions
 */
private fun getTransitions(
    stateId: StateId,
    fsm: NDFSM,
): List<Transition> =
    fsm.transitions.getValue(stateId).flatMap { t ->
        when (t) {
            is Epsilon -> getTransitions(t.cont, fsm)
            is TransitionWithAction -> listOf(Transition(t.action, t.cont))
        }
    }


/**
 * Calculates the transitions available to a State, following epsilon transitions
 */
private fun reachable(
    reachable: Set<StateId>,
    transitions: Map<StateId, List<Transition>>,
): Set<StateId> {
    val newStates = reachable.flatMap { id ->
        transitions.getOrDefault(id, emptyList()).map { it.cont }
    }.toSet()

    return if (newStates.minus(reachable).isNotEmpty()) {
        // we have new states
        reachable(reachable.plus(newStates), transitions)
    } else {
        reachable
    }
}


//
//            transitions.g s.flatMap { t ->
//                when (t) {
//                    is Epsilon -> getTransitions(states.find { it.id == t.cont }!!, states)
//                    is TransitionWithAction -> listOf(t)
//                }
//            }


/**
 * Create a simplified graph by removing epsilon transitions and unreachable states
 */
//private fun simplify(states: Set<State>): Set<State> =
//    states.map {
//        when (it) {
//            EndState -> EndState
//            else -> State(it.id, getTransitions(it, states).toMutableList())
//        }
//    }.toSet()

//        private fun successors(s: NaiveState, states: Set<NaiveState>) {
//            return successors(mutableSetOf(s), states, mutableSetOf())
//        }
//
//        private fun successors(initialStates: MutableSet<NaiveState>, states: Set<NaiveState>, reachable: Set<Int>) {
//            val newSet = mutableSetOf<NaiveState>()
//            initialStates.forEach { s ->
//                newSet.addAll(s.transitions.map { memoit.cont })
//            }
//        }
//

