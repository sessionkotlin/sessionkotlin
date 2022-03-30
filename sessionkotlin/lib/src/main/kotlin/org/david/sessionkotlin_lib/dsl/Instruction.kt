package org.david.sessionkotlin_lib.dsl

import org.david.sessionkotlin_lib.dsl.exception.SendingtoSelfException
import org.david.sessionkotlin_lib.dsl.util.getOrKey
import org.david.sessionkotlin_lib.dsl.util.printlnIndent


internal sealed interface Instruction {
    fun dump(indent: Int)
    fun mapped(mapping: Map<Role, Role>): Instruction
}

internal sealed interface TerminalInstruction : Instruction {
    fun simpleName(): String
}

internal data class Send(
    internal val from: Role,
    internal val to: Role,
    internal val type: Class<*>,
) : Instruction {

    init {
        if (from == to) {
            throw SendingtoSelfException(from)
        }
    }

    override fun dump(indent: Int) {
        printlnIndent(indent, "Send<${type.simpleName}>[$from -> $to]")
    }

    override fun mapped(mapping: Map<Role, Role>): Instruction =
        Send(
            mapping.getOrKey(from),
            mapping.getOrKey(to),
            type
        )
}

internal class Choice(
    internal val at: Role,
    internal val caseMap: MutableMap<String, GlobalEnv>,
) : TerminalInstruction {
    override fun simpleName(): String = "Choice at $at"

    override fun dump(indent: Int) {

        printlnIndent(indent, "Choice[$at, cases: ")
        for (c in caseMap) {
            printlnIndent(indent, "${c.key}:")
            c.value.dump(indent + 2)
        }

        printlnIndent(indent, "]")
    }


    override fun mapped(mapping: Map<Role, Role>): Instruction {

        val newCaseMap = mutableMapOf<String, GlobalEnv>()
        for (case in caseMap) {
            val newRoles = case.value.roles.map { mapping.getOrKey(it) }.toSet()
            val newCaseValue = NonRootEnv(newRoles, case.value.recursionVariables)

            val newInstructions = case.value.instructions.map { it.mapped(mapping) }
            newCaseValue.instructions = newInstructions.toMutableList()

            newCaseMap[case.key] = newCaseValue
        }

        return Choice(mapping.getOrKey(at), newCaseMap)
    }

}

internal class RecursionDefinition(internal val tag: RecursionTag) : Instruction {
    override fun dump(indent: Int) {
        printlnIndent(indent, "miu_$tag")
    }

    override fun mapped(mapping: Map<Role, Role>): Instruction =
        RecursionDefinition(tag)
}

internal class Recursion(internal val tag: RecursionTag) : TerminalInstruction {
    override fun simpleName(): String = "Recursion($tag)"
    override fun dump(indent: Int) {
        printlnIndent(indent, "$tag")
    }

    override fun mapped(mapping: Map<Role, Role>): Instruction =
        Recursion(tag)
}