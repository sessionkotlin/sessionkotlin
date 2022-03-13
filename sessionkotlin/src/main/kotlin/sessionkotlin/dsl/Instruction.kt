package sessionkotlin.dsl

import sessionkotlin.dsl.exception.InconsistentExternalChoiceException
import sessionkotlin.dsl.exception.RoleNotEnabledException
import sessionkotlin.dsl.exception.SendingtoSelfException
import java.util.*
import javax.swing.text.html.Option


internal abstract class Instruction {
    abstract fun dump(indent: Int)
}

internal class Send<T>(
    private val from: Role,
    private val to: Role
) : Instruction() {

    override fun dump(indent: Int) {
        printlnIndent(indent, "Send[$from -> $to]")
    }
}

internal class Branch(
    private val at: Role,
    private val caseMap: MutableMap<String, GlobalEnv>
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

private fun printlnIndent(indent: Int, message: Any?) {
    println(" ".repeat(indent) + message)
}

internal class Rec: Instruction() {
    override fun dump(indent: Int) {
        printlnIndent(indent, "Recursive call")
    }
}