package sessionkotlin.dsl

import sessionkotlin.dsl.exception.InconsistentExternalChoiceException
import sessionkotlin.dsl.exception.RoleNotEnabledException
import sessionkotlin.dsl.exception.SendingtoSelfException


internal abstract class Interaction(
    internal val initiator: Role,
    internal val target: Role?,
) {
    abstract fun dump(indent: Int)
    abstract fun with(mapping: Map<Role, Role>): Interaction
}

internal class Send<T>(
    private val from: Role,
    private val to: Role,
) : Interaction(from, to) {

    init {
        if (from == to) {
            throw SendingtoSelfException(from)
        }
    }

    override fun dump(indent: Int) {
        printlnIndent(indent, "Send[$from -> $to]")
    }

    override fun with(mapping: Map<Role, Role>): Interaction {
        val newFrom = mapping[from] ?: from
        val newTo = mapping[to] ?: to
        return Send<T>(newFrom, newTo)
    }

}

internal class Branch(
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

    override fun dump(indent: Int) {

        printlnIndent(indent, "Choice[$at, cases: ")
        for (c in caseMap) {
            printlnIndent(indent, "${c.key}:")
            c.value.dump(indent + 2)
        }

        printlnIndent(indent, "]")
    }

    override fun with(mapping: Map<Role, Role>): Interaction {
        val newAt = mapping[at] ?: at
        return Branch(newAt, caseMap)
    }
}

private fun printlnIndent(indent: Int, message: Any?) {
    println(" ".repeat(indent) + message)
}