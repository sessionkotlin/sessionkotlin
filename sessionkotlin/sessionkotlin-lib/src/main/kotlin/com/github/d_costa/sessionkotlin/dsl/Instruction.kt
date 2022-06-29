package com.github.d_costa.sessionkotlin.dsl

import com.github.d_costa.sessionkotlin.dsl.exception.SendingToSelfException
import com.github.d_costa.sessionkotlin.util.printlnIndent

internal sealed interface Instruction {
    fun dump(indent: Int)
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
    internal val msgLabel: String,
    internal val condition: String,
) : Instruction {

    init {
        if (from == to) {
            throw SendingToSelfException(from)
        }
    }

    override fun dump(indent: Int) {
        printlnIndent(indent, "Send($msgLabel)<${type.simpleName}>[$from -> $to]")
    }
}

internal class Choice(
    internal val at: SKRole,
    internal val branches: MutableList<GlobalEnv>,
) : TerminalInstruction {
    override fun simpleName(): String = "Choice at $at"

    override fun dump(indent: Int) {

        printlnIndent(indent, "Choice[$at, branches: ")
        for (c in branches) {
            printlnIndent(indent, "{")
            c.dump(indent + 2)
            printlnIndent(indent, "}")
        }

        printlnIndent(indent, "]")
    }
}

internal class RecursionDefinition(internal val tag: RecursionTag) : Instruction {
    override fun dump(indent: Int) {
        printlnIndent(indent, "mu_$tag")
    }
}

internal class Recursion(internal val tag: RecursionTag) : TerminalInstruction {
    override fun simpleName(): String = "Recursion($tag)"
    override fun dump(indent: Int) {
        printlnIndent(indent, "$tag")
    }
}
