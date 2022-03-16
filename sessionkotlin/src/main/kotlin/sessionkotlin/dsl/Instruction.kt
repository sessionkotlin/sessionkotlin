package sessionkotlin.dsl

import sessionkotlin.dsl.exception.UnfinishedRolesException


internal abstract class Instruction {
    abstract fun dump(indent: Int)
}

internal class Send<T>(
    private val from: Role,
    private val to: Role,
) : Instruction() {

    override fun dump(indent: Int) {
        printlnIndent(indent, "Send[$from -> $to]")
    }
}

internal class Branch(
    private val at: Role,
    private val caseMap: MutableMap<String, GlobalEnv>,
) : Instruction() {

    init {
        val counters = mutableMapOf<Role, Int>()
        for (c in caseMap.values) {
            for (r in c.activations.keys)
                if (r != at) {
                    counters.merge(r, 1, Int::plus)
                }
        }
        val unfinishedRoles =
            counters
                .filterValues { it != 0 && it != caseMap.size }
                .keys
        if (unfinishedRoles.isNotEmpty()) {
            throw UnfinishedRolesException(unfinishedRoles)
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
}

internal fun printlnIndent(indent: Int, message: Any?) {
    println(" ".repeat(indent) + message)
}

internal class Rec : Instruction() {
    override fun dump(indent: Int) {
        printlnIndent(indent, "Recursive call")
    }
}