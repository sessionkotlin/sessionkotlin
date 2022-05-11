package com.github.d_costa.sessionkotlin.dsl

import com.github.d_costa.sessionkotlin.dsl.exception.DuplicateBranchLabelException
import com.github.d_costa.sessionkotlin.dsl.exception.InvalidBranchLabelException

@SessionKotlinDSL
public class ChoiceEnv(
    private val roles: Set<SKRole>,
    private val recursionVariables: Set<RecursionTag>,
) {
    /**
     * Maps labels to global protocols.
     */
    internal val branchMap = mutableMapOf<String, GlobalEnv>()

    /**
     * Add a branch to a choice.
     *
     * @param label the branch label
     * @param protocolBuilder protocol definition for the branch
     */
    public fun branch(label: String, protocolBuilder: GlobalEnv.() -> Unit) {
        val p = NonRootEnv(roles, recursionVariables)
        p.protocolBuilder()
        if (branchMap.containsKey(label)) {
            throw DuplicateBranchLabelException(label)
        }
        if (hasWhitespace(label)) {
            throw InvalidBranchLabelException(label)
        }
        branchMap[label] = p
    }

    private fun hasWhitespace(label: String) =
        label.any { it.isWhitespace() }
}
