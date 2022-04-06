package org.david.sessionkotlin_lib.dsl

import org.david.sessionkotlin_lib.dsl.exception.DuplicateCaseLabelException

@SessionKotlinDSL
public class ChoiceEnv(
    private val roles: Set<Role>,
    private val recursionVariables: Set<RecursionTag>,
) {
    internal val caseMap = mutableMapOf<String, GlobalEnv>()

    public fun case(label: String, protocolBuilder: GlobalEnv.() -> Unit) {
        val p = NonRootEnv(roles, recursionVariables)
        p.protocolBuilder()
        if (caseMap.containsKey(label)) {
            throw DuplicateCaseLabelException(label)
        }
        caseMap[label] = p
    }
}
