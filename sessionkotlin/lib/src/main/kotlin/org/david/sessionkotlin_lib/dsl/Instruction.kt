package org.david.sessionkotlin_lib.dsl


internal sealed interface Instruction {
    fun dump(indent: Int)
}

internal sealed interface TerminalInstruction : Instruction {
    fun simpleName(): String
}

internal data class Send(
    internal val from: Role,
    internal val to: Role,
    internal val type: Class<*>,
) : Instruction {

    override fun dump(indent: Int) {
        printlnIndent(indent, "Send<${type.simpleName}>[$from -> $to]")
    }
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
}

internal class RecursionDefinition(internal val tag: RecursionTag) : Instruction {
    override fun dump(indent: Int) {
        printlnIndent(indent, "miu_$tag")
    }
}

internal class Recursion(internal val tag: RecursionTag) : TerminalInstruction {
    override fun simpleName(): String = "Recursion($tag)"
    override fun dump(indent: Int) {
        printlnIndent(indent, "$tag")
    }
}