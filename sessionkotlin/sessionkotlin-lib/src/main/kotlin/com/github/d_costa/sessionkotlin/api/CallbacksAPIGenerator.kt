package com.github.d_costa.sessionkotlin.api

import com.github.d_costa.sessionkotlin.dsl.RootEnv
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.fsm.*
import com.squareup.kotlinpoet.*

internal class CallbacksAPIGenerator(globalEnv: RootEnv) : NewAPIGenerator(globalEnv, "callbacks") {

    init {
        roleMap.keys.forEach {
            val fsm = fsmFromLocalType(globalEnv.project(it))
            val file = generateCallbacksAPI(fsm, it)
            files.add(file)
        }
    }

    private fun generateCallbacksAPI(fsm: FSM, role: SKRole): FileSpec {
        val file = newFile(buildClassname(role))

        for (state in fsm.states) {
//            if (state.id == FSM.endStateIndex)
//                continue
//            val stateTransitions = fsm.transitions.getOrDefault(state.id, emptyList())
//            val stateClasses = generateStateClasses(state, stateTransitions, role)
//
//            for (s in stateClasses)
//                file.addType(s)
        }
        return file.build()
    }
}
