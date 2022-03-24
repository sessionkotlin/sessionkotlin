package org.david.sessionkotlin_lib.dsl


internal sealed class Instruction {
    abstract fun dump(indent: Int)
}

internal class Send(
    internal val from: Role,
    internal val to: Role,
    internal val type: Class<*>,
) : Instruction() {

    override fun dump(indent: Int) {
        printlnIndent(indent, "Send<${type.simpleName}>[$from -> $to]")
    }

    override fun equals(other: Any?): Boolean {
        if (other is Send) {
            return from == other.from && to == other.to && type == other.type
        }
        return false
    }

    override fun hashCode(): Int {
        // auto generated
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + type.hashCode()
        return result
    }
}

internal class Branch(
    internal val at: Role,
    internal val caseMap: MutableMap<String, GlobalEnv>,
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

internal class Rec : Instruction() {
    override fun dump(indent: Int) {
        printlnIndent(indent, "Recursive call")
    }
}