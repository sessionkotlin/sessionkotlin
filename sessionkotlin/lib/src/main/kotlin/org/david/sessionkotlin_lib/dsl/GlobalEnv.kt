package org.david.sessionkotlin_lib.dsl

import org.david.sessionkotlin_lib.dsl.exception.RecursiveProtocolException
import org.david.sessionkotlin_lib.dsl.exception.SendingtoSelfException

@DslMarker
private annotation class SessionKotlinDSL

@SessionKotlinDSL
abstract class GlobalEnv(
    roles: Set<Role>
) {
    internal val instructions = mutableListOf<Instruction>()
    private val roles = roles.toMutableSet()
    private var recursiveCall = false

    /**
     *
     * Declares that [from] should send a message of type [T] to [to].
     *
     * @param [from] role that sends the message
     * @param [to] role that receives the message
     *
     * @throws [dsl.exception.SendingtoSelfException]
     * if [from] and [to] are the same.
     *
     * @throws [dsl.exception.RecursiveProtocolException]
     * if used after a recursive call.
     *
     * @sample [dsl.Samples.send]
     *
     */
    open fun <T> send(from: Role, to: Role) {
        if (recursiveCall) {
            throw RecursiveProtocolException()
        }
        if (from == to) {
            throw SendingtoSelfException(from)
        }

        val msg = Send<T>(from, to)
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
     * @throws [dsl.exception.RecursiveProtocolException]
     * if used after a recursive call.
     *
     * @sample [dsl.Samples.choice]
     *
     */
    open fun choice(at: Role, cases: ChoiceEnv.() -> Unit) {
        if (recursiveCall) {
            throw RecursiveProtocolException()
        }

        val bEnv = ChoiceEnv(roles, setOf(at))
        bEnv.cases()
        val b = Branch(at, bEnv.caseMap)
        instructions.add(b)
        roles.add(at)
    }

    /**
     *
     * Appends a global protocol.
     *
     * @param [protocolBuilder] protocol to append
     *
     * @sample [dsl.Samples.exec]
     *
     */
    open fun exec(protocolBuilder: GlobalEnv) {
        if (recursiveCall) {
            throw RecursiveProtocolException()
        }

        // We must merge the protocols
        recursiveCall = protocolBuilder.recursiveCall
        instructions.addAll(protocolBuilder.instructions)
    }

    /**
     *
     * Declares a recursive call.
     *
     * @throws [dsl.exception.RecursiveProtocolException]
     * if used after a recursive call.
     *
     * @sample [dsl.Samples.rec]
     *
     */
    open fun rec() {
        if (recursiveCall) {
            throw RecursiveProtocolException()
        }

        val msg = Rec()
        instructions.add(msg)
        recursiveCall = true
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
}

internal class RootEnv : GlobalEnv(emptySet())

internal class NonRootEnv(
    roles: Set<Role>
) : GlobalEnv(roles)

@SessionKotlinDSL
class ChoiceEnv(
    private val roles: Set<Role>,
    private val enabledRoles: Set<Role>,
) {
    internal val caseMap = mutableMapOf<String, GlobalEnv>()

    fun case(label: String, protocolBuilder: GlobalEnv.() -> Unit) {
        val p = NonRootEnv(roles)
        p.protocolBuilder()
        caseMap[label] = p
    }
}

fun globalProtocol(protocolBuilder: GlobalEnv.() -> Unit): GlobalEnv {
    val p = RootEnv()
    p.protocolBuilder()
    return p
}