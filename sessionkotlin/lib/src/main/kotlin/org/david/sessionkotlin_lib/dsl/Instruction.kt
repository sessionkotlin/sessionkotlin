package org.david.sessionkotlin_lib.dsl


internal abstract class Instruction {
    abstract fun dump(indent: Int)
}

internal class Send(
    private val from: Role,
    private val to: Role,
    private val type: Class<*>,
) : Instruction() {

    override fun dump(indent: Int) {
        printlnIndent(indent, "Send<$type>[$from -> $to]")
    }
}

internal class Branch(
    private val at: Role,
    private val caseMap: MutableMap<String, GlobalEnv>,
) : Instruction() {

    override fun dump(indent: Int) {

        printlnIndent(indent, "Choice[$at, cases: ")
        for (c in caseMap) {
            printlnIndent(indent, "${c.key}:")
            c.value.dump(indent + 2)
        }

        printlnIndent(indent, "]")
    }
}

internal fun printlnIndent(indent: Int, message: Any?) {
    println(" ".repeat(indent) + message)
}

internal class Rec : Instruction() {
    override fun dump(indent: Int) {
        printlnIndent(indent, "Recursive call")
    }
}