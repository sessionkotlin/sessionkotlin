package com.github.sessionkotlin.lib.dsl

@SessionKotlinDSL
public class ChoiceEnv(
    private val roles: Set<SKRole>,
    private val recursionVariables: Set<RecursionTag>,
) {
    /**
     * Maps labels to global protocols.
     */
    internal val branchMap = mutableListOf<GlobalEnv>()

    /**
     * Add a branch to a choice.
     *
     * @param protocolBuilder protocol definition for the branch
     */
    public fun branch(protocolBuilder: GlobalEnv.() -> Unit) {
        val p = NonRootEnv(roles, recursionVariables)
        p.protocolBuilder()
        branchMap.add(p)
    }
}
