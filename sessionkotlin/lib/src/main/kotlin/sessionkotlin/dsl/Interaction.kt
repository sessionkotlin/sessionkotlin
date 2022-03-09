package sessionkotlin.dsl

import sessionkotlin.dsl.exception.InconsistentExternalChoiceException
import sessionkotlin.dsl.exception.RoleNotEnabledException


abstract class Interaction(
    internal val initiator: Role,
    internal val target: Role?,
) {
    abstract fun dump()
}

class Send<T>(
    private val from: Role,
    private val to: Role,
) : Interaction(from, to) {

    override fun dump() {
        println("Send[$from -> $to]")
    }

}

class Branch(
    private val at: Role,
    private val caseMap: MutableMap<String, GlobalEnv>,
) : Interaction(at, null) {

    init {
        val activators = mutableMapOf<Role, Set<Role>>()

        for (case in caseMap.values) {

            val activatedRoles = mutableSetOf(at)
            for (interaction in case.interactions) {

                // Verify that only activated roles intiate interactions
                if (interaction.initiator in activatedRoles) {
                    if (interaction.target != null) {
                        val added = activatedRoles.add(interaction.target)

                        if (added) {
                            // [interaction.target] was activated by [interaction.initiator]
                            activators.merge(interaction.target, setOf(interaction.initiator), Set<Role>::plus)
                        }
                    }
                } else {
                    throw RoleNotEnabledException(interaction.initiator)
                }
            }
        }
        // Verify that external choices are consistent
        for ((role, actv) in activators) {
            if (actv.isEmpty()) {
                throw RoleNotEnabledException(role)
            }
            if (actv.size > 1) {
                throw InconsistentExternalChoiceException(role, actv)
            }
        }
    }

    override fun dump() {
        println("Branch[$at, cases: ")

        for (c in caseMap) {
            println("${c.key}:")
            c.value.debug()
        }

        println("]")
    }
}