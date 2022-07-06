package com.github.d_costa.sessionkotlin.fsm

import com.github.d_costa.sessionkotlin.api.exception.NoInitialStateException
import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.NonDeterministicStatesException
import com.github.d_costa.sessionkotlin.dsl.types.*
import com.github.d_costa.sessionkotlin.util.mapMutable
import com.github.d_costa.sessionkotlin.util.merge

internal typealias LocalTypeId = Int

/**
 * Create a Finite State Automata from a LocalType representation.
 */
internal fun statesFromLocalType(localType: LocalType): List<State> {
    /*
     * When a transition is created, it points to a LocalTypeId.
     * After the automata is built, translateIds() translates LocalTypeIds to StateIds.
     *
     */

    fun LocalType.id() = hashCode()

    /**
     * Map [RecursionTag]s to [LocalType] ids
     */
    val recursions = mutableMapOf<RecursionTag, LocalTypeId>()

    /**
     * Map [LocalType] ids to a [SimpleState] ids
     */
    val memo = mutableMapOf<LocalTypeId, SimpleState>(Pair(LocalTypeEnd.id(), EndSimpleState))

    val states = mutableSetOf<SimpleState>(EndSimpleState)
    val stateTransitions = mutableMapOf<Int, MutableList<NDTransition>>()
    var index = State.initialStateIndex

    fun buildStates(l: LocalType) {
        if (l.id() in memo) {
            // Skip
            return
        }

        fun createState(): StateId {
            val s = SimpleState(index++)
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
     * Replace LocalTypeIds with StateIds
     */
    fun translateIds(transitions: Map<Int, List<NDTransition>>) =
        transitions.mapValues { (_, v) ->
            v.map {
                when (it) {
                    is Epsilon -> Epsilon(memo.getValue(it.cont).id)
                    is TransitionWithAction -> TransitionWithAction(it.action, memo.getValue(it.cont).id)
                }
            }
        }

    buildStates(localType)
    val naiveFSM = NDFSM(states, translateIds(stateTransitions))
    return simplify(naiveFSM)
}

/**
 * Create a simplified graph by removing epsilon transitions and unreachable states
 */
private fun simplify(fsm: NDFSM): List<State> {
    val transitionsWithoutEpsilon = fsm.transitions.mapValues { (k, _) ->
        getTransitions(k, fsm)
    }

    val initialState = fsm.states.find { it.id == State.initialStateIndex } ?: throw NoInitialStateException()
    val reachableStateIds: Set<StateId> = reachable(setOf(initialState.id), transitionsWithoutEpsilon)

    val states = fsm.states.filter { reachableStateIds.contains(it.id) }.toSet()
    val transitions = transitionsWithoutEpsilon.filterKeys { reachableStateIds.contains(it) }

    val pair = removeRedundantStates(states, transitions)

    val simplePair = simplifyStateIds(pair.first, pair.second)

    validateDeterminism(simplePair.first, simplePair.second)

    return toRichStates(simplePair.first, simplePair.second)
}

private fun removeRedundantStates(
    states: Set<SimpleState>,
    transitions: StateTransitions,
): Pair<List<SimpleState>, StateTransitions> {
    /**
     * Maps a redundant state to its substitute
     */
    val redundant = mutableMapOf<StateId, StateId>()

    val entries = mutableMapOf<List<SimpleTransition>, MutableList<StateId>>()
    for (s in states) {
        val ts = transitions.getOrDefault(s.id, emptyList())
        entries.merge(ts, s.id)
    }

    for ((_, ids) in entries) {
        if (ids.size > 1) {
            // Keep smallest id
            val stateToKeep = ids.minByOrNull { it }!! // size > 1
            val redundantStates = ids.filter { it != stateToKeep }

            for (r in redundantStates) {
                redundant[r] = stateToKeep
            }
        }
    }

    val newTransitions = transitions.mapValues { (_, ts) ->
        ts.map {
            val substitute = redundant[it.cont]
            if (substitute != null) {
                it.copy(cont = substitute)
            } else it
        }
    }
    val newStates = states.filter { it.id !in redundant }
    return Pair(newStates, newTransitions)
}

private fun simplifyStateIds(
    states: List<SimpleState>,
    transitions: StateTransitions,
): Pair<List<SimpleState>, StateTransitions> {

    val substitutions = mutableMapOf<StateId, StateId>()

    var counter = State.initialStateIndex
    for (s in states) {
        if (s.id == State.endStateIndex) continue

        if (s.id > counter) {
            substitutions[s.id] = counter
        }
        counter++
    }

    val newTransitions = transitions.mapValues { (_, ts) ->
        ts.map {
            val substitute = substitutions[it.cont]
            if (substitute != null) {
                it.copy(cont = substitute)
            } else it
        }
    }.mapKeys { (id, _) -> substitutions[id] ?: id }

    val newStates = states.map {
        val substitute = substitutions[it.id]
        if (substitute != null) {
            SimpleState(substitute)
        } else it
    }
    return Pair(newStates, newTransitions)
}

private fun validateDeterminism(states: List<SimpleState>, transitions: StateTransitions) {
    for (s in states) {
        val ts = transitions.getOrElse(s.id, ::emptyList)
        val dupeLabels = ts
            .groupingBy { it.action.label.name }.eachCount().filter { it.value > 1 }

        if (dupeLabels.isNotEmpty() && ts.any { it.action is ReceiveAction }) {
            throw NonDeterministicStatesException(ts.filter { it.action.label.name in dupeLabels })
        }
    }
}

/**
 * Calculates the transitions available to a State, following epsilon transitions
 */
private fun getTransitions(
    stateId: StateId,
    fsm: NDFSM,
): List<SimpleTransition> = getTransitionsRecursive(stateId, stateId, fsm, 0)

/**
 * Saving [stateId] prevents stack overflows; Knowing the index [i] allows us to capture direct loops to self.
 */
private fun getTransitionsRecursive(
    stateId: StateId, // the original
    currId: StateId,
    fsm: NDFSM,
    i: Long = 0,
): List<SimpleTransition> =
    fsm.transitions.getValue(currId).flatMap { t ->
        when (t) {
            is Epsilon -> if (t.cont == stateId && i > 0) emptyList() else getTransitionsRecursive(
                stateId,
                t.cont,
                fsm,
                i + 1
            )
            is TransitionWithAction -> listOf(SimpleTransition(t.action, t.cont))
        }
    }

/**
 * Calculates the transitions available to a State, following epsilon transitions
 */
private fun reachable(
    reachable: Set<StateId>,
    transitions: StateTransitions,
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

private fun commonSource(transitions: List<SimpleTransition>): SKRole {
    val sources = transitions.map { (it.action as ReceiveAction).from }.toSet()
    if (sources.size > 1)
        throw RuntimeException("Inconsistent external choice: [${sources.joinToString()}]")
    return sources.first()
}

private fun toRichStates(states: List<SimpleState>, transitions: StateTransitions): List<State> =
    states.map { s ->
        if (s is EndSimpleState) return@map EndState

        val ts = transitions.getValue(s.id)
        if (ts.all { it.action is ReceiveAction }) {
            if (ts.size == 1) {
                val t = ts.first()
                ReceiveState(s.id, ReceiveTransition(t.action as ReceiveAction, t.cont))
            } else {
                ExternalChoiceState(
                    s.id,
                    commonSource(ts),
                    ts.map { ReceiveTransition(it.action as ReceiveAction, it.cont) }
                )
            }
        } else {
            if (ts.size == 1) {
                val t = ts.first()
                SendState(s.id, SendTransition(t.action as SendAction, t.cont))
            } else {
                InternalChoiceState(
                    s.id,
                    ts.map {
                        when (it.action) {
                            is SendAction -> SendTransition(it.action, it.cont)
                            is ReceiveAction -> ReceiveTransition(it.action, it.cont)
                        }
                    }
                )
            }
        }
    }
