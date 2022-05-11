package com.github.d_costa.sessionkotlin.dsl

import com.github.d_costa.sessionkotlin.dsl.exception.SendingToSelfException
import com.github.d_costa.sessionkotlin.util.getOrKey
import com.github.d_costa.sessionkotlin.util.printlnIndent

internal sealed interface Instruction {
    fun dump(indent: Int)
    fun mapped(mapping: Map<SKRole, SKRole>): Instruction
}

/**
 * An instruction that terminates the protocol.
 */
internal sealed interface TerminalInstruction : Instruction {
    fun simpleName(): String
}

internal data class Send(
    internal val from: SKRole,
    internal val to: SKRole,
    internal val type: Class<*>,
    internal val msgLabel: String?,
    internal val condition: String
) : Instruction {

    init {
        if (from == to) {
            throw SendingToSelfException(from)
        }
    }

    override fun dump(indent: Int) {
        printlnIndent(indent, "Send($msgLabel)<${type.simpleName}>[$from -> $to]")
    }

    override fun mapped(mapping: Map<SKRole, SKRole>): Instruction =
        Send(
            mapping.getOrKey(from),
            mapping.getOrKey(to),
            type,
            msgLabel,
            condition
        )
}

internal class Choice(
    internal val at: SKRole,
    internal val branchMap: MutableMap<String, GlobalEnv>,
) : TerminalInstruction {
    override fun simpleName(): String = "Choice at $at"

    override fun dump(indent: Int) {

        printlnIndent(indent, "Choice[$at, branches: ")
        for (c in branchMap) {
            printlnIndent(indent, "${c.key}:")
            c.value.dump(indent + 2)
        }

        printlnIndent(indent, "]")
    }

    override fun mapped(mapping: Map<SKRole, SKRole>): Instruction {

        val newBranchMap = mutableMapOf<String, GlobalEnv>()
        for (branch in branchMap) {
            val newRoles = branch.value.roles.map { mapping.getOrKey(it) }.toSet()
            val newBranchValue = NonRootEnv(newRoles, branch.value.recursionVariables)

            val newInstructions = branch.value.instructions.map { it.mapped(mapping) }
            newBranchValue.instructions = newInstructions.toMutableList()

            newBranchMap[branch.key] = newBranchValue
        }

        return Choice(mapping.getOrKey(at), newBranchMap)
    }
}

internal class RecursionDefinition(internal val tag: RecursionTag) : Instruction {
    override fun dump(indent: Int) {
        printlnIndent(indent, "miu_$tag")
    }

    override fun mapped(mapping: Map<SKRole, SKRole>): Instruction =
        RecursionDefinition(tag)
}

internal class Recursion(internal val tag: RecursionTag) : TerminalInstruction {
    override fun simpleName(): String = "Recursion($tag)"
    override fun dump(indent: Int) {
        printlnIndent(indent, "$tag")
    }

    override fun mapped(mapping: Map<SKRole, SKRole>): Instruction =
        Recursion(tag)
}
