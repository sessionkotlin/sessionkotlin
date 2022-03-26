package org.david.sessionkotlin_lib.dsl

import org.david.sessionkotlin_lib.dsl.exception.SendingtoSelfException
import org.david.sessionkotlin_lib.dsl.exception.SessionKotlinException
import org.david.sessionkotlin_lib.dsl.exception.TerminalInstructionException
import org.david.sessionkotlin_lib.dsl.exception.UndefinedRecursionVariableException
import org.david.sessionkotlin_lib.dsl.types.*

@DslMarker
private annotation class SessionKotlinDSL

class RecursionTag internal constructor(
    private val name: String,
) {
    override fun toString(): String = name
}

@SessionKotlinDSL
sealed class GlobalEnv(
    roles: Set<Role>,
    recursionVariables: Set<RecursionTag>,
) {
    internal val instructions = mutableListOf<Instruction>()
    private val roles = roles.toMutableSet()
    private val recursionVariables = recursionVariables.toMutableSet()

    /**
     *
     * Declares that [from] should send a message of type [T] to [to].
     *
     * As this function uses a reified type, it is not callable from Java.
     * If you are using Java, use the alternative declaration:
     *
     * <nobr>`send(from, to, type)`<nobr>
     *
     * @param [from] role that sends the message
     * @param [to] role that receives the message
     *
     * @throws [org.david.sessionkotlin_lib.dsl.exception.SendingtoSelfException]
     * if [from] and [to] are the same.
     *
     *
     * @sample [org.david.sessionkotlin_lib.dsl.Samples.send]
     *
     */
    inline fun <reified T> send(from: Role, to: Role) {
        send(from, to, T::class.java)
    }

    /**
     *
     * Declares that [from] should send a message of type [type] to [to].
     *
     * @param [from] role that sends the message
     * @param [to] role that receives the message
     *
     * @throws [org.david.sessionkotlin_lib.dsl.exception.SendingtoSelfException]
     * if [from] and [to] are the same.
     *
     *
     * @sample [org.david.sessionkotlin_lib.dsl.Samples.sendTypes]
     *
     */
    open fun send(from: Role, to: Role, type: Class<*>) {
        if (from == to) {
            throw SendingtoSelfException(from)
        }

        val msg = Send(from, to, type)
        roles.add(from)
        roles.add(to)

        instructions.add(msg)
    }

    /**
     *
     * Declares an internal choice at [at].
     *
     * @param [at] role that makes the decision
     * @param [cases] block that defines the choices
     *
     *
     * @sample [org.david.sessionkotlin_lib.dsl.Samples.choice]
     *
     */
    open fun choice(at: Role, cases: ChoiceEnv.() -> Unit) {
        val bEnv = ChoiceEnv(roles, recursionVariables)
        bEnv.cases()
        val b = Choice(at, bEnv.caseMap)

        roles.add(at)
        for (g in b.caseMap.values) {
            roles.addAll(g.roles)
        }
        instructions.add(b)
    }

    /**
     *
     * Appends a global protocol.
     *
     * @param [protocolBuilder] protocol to append
     *
     * @sample [org.david.sessionkotlin_lib.dsl.Samples.exec]
     *
     */
    open fun exec(protocolBuilder: GlobalEnv) {
        // We must merge the protocols
        instructions.addAll(protocolBuilder.instructions)
        roles.addAll(protocolBuilder.roles)
    }

    /**
     *
     * Recursion definition.
     *
     *
     * @sample [org.david.sessionkotlin_lib.dsl.Samples.goto]
     *
     * @return a [RecursionTag] to be used in [goto] calls.
     *
     */
    open fun miu(name: String): RecursionTag {
        val tag = RecursionTag(name)
        val msg = RecursionDefinition(tag)
        instructions.add(msg)
        recursionVariables.add(tag)
        return tag
    }

    /**
     *
     * Recursion point.
     *
     * @param [tag] the tag of the recursion point to go to.
     *
     * @sample [org.david.sessionkotlin_lib.dsl.Samples.goto]
     *
     */
    open fun goto(tag: RecursionTag) {
        if (!recursionVariables.contains(tag)) {
            throw UndefinedRecursionVariableException(tag)
        }
        val msg = Recursion(tag)
        instructions.add(msg)
    }

    /**
     * Prints the protocol to the standard output.
     */
    fun dump(indent: Int = 0) {
        printlnIndent(indent, "{")
        for (i in instructions) {
            i.dump(indent)
        }
        printlnIndent(indent, "}")
    }

    internal fun project(role: Role): LocalType {
//        for (i in instructions)
//            i.dump(0)
        return buildGlobalType(instructions)
            .project(role, State())
    }

    fun validate() {
        roles.forEach {
            try {
                project(it)
            } catch (e: SessionKotlinException) {
                System.err.println("Exception while projecting $it:")
                throw e
            }
        }
    }
}


internal fun buildGlobalType(
    instructions: MutableList<Instruction>,
): GlobalType =
    if (instructions.isEmpty()) {
        GlobalTypeEnd
    } else {
        val head = instructions.first()
        val tail = instructions.subList(1, instructions.size)

        if (head is TerminalInstruction && tail.isNotEmpty()) {
            throw TerminalInstructionException(head)
        }

        when (head) {
            is Send -> GlobalTypeSend(head.from, head.to, head.type, buildGlobalType(tail))
            is Choice -> GlobalTypeBranch(head.at, head.caseMap.mapValues { buildGlobalType(it.value.instructions) })
            is Recursion -> GlobalTypeRecursion(head.tag)
            is RecursionDefinition -> GlobalTypeRecursionDefinition(head.tag, buildGlobalType(tail))
        }
    }

internal class RootEnv : GlobalEnv(emptySet(), emptySet())

internal class NonRootEnv(
    roles: Set<Role>, recursionVariables: Set<RecursionTag>,
) : GlobalEnv(roles, recursionVariables)

@SessionKotlinDSL
class ChoiceEnv(
    private val roles: Set<Role>,
    private val recursionVariables: Set<RecursionTag>,
) {
    internal val caseMap = mutableMapOf<String, GlobalEnv>()

    fun case(label: String, protocolBuilder: GlobalEnv.() -> Unit) {
        val p = NonRootEnv(roles, recursionVariables)
        p.protocolBuilder()
        caseMap[label] = p
    }
}

fun globalProtocol(protocolBuilder: GlobalEnv.() -> Unit): GlobalEnv {
    val p = RootEnv()
    p.protocolBuilder()
    p.validate()
    return p
}