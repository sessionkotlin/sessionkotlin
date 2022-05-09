package org.david.sessionkotlin.dsl

import org.david.sessionkotlin.api.generateAPI
import org.david.sessionkotlin.dsl.exception.*
import org.david.sessionkotlin.dsl.types.*
import org.david.sessionkotlin.parser.RefinementParser
import org.david.sessionkotlin.util.getOrKey
import org.david.sessionkotlin.util.printlnIndent

@SessionKotlinDSL
public sealed class GlobalEnv(
    roles: Set<SKRole>,
    recursionVariables: Set<RecursionTag>,
) {
    internal var instructions = mutableListOf<Instruction>()
    internal val roles = roles.toMutableSet()
    internal val recursionVariables = recursionVariables.toMutableSet()
    internal val msgLabels = mutableSetOf<String>()

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
     * @param [label] message label. Required if using callbacks API.
     *
     * @throws [org.david.sessionkotlin.dsl.exception.SendingToSelfException]
     * if [from] and [to] are the same.
     * @throws [DuplicateMessageLabelException] if [label] is not null and not unique in this protocol.
     *
     *
     * @sample [org.david.sessionkotlin.dsl.Samples.send]
     *
     */
    public inline fun <reified T> send(
        from: SKRole,
        to: SKRole,
        label: String? = null,
        condition: String = "",
    ): Unit = send(from, to, T::class.java, label, condition)

    /**
     *
     * Declares that [from] should send a message of type [type] to [to].
     *
     * @param [from] role that sends the message
     * @param [to] role that receives the message
     *
     * @throws [org.david.sessionkotlin.dsl.exception.SendingToSelfException]
     * if [from] and [to] are the same.
     * @throws [DuplicateMessageLabelException] if [label] is not null and not unique in this protocol.
     *
     * @sample [org.david.sessionkotlin.dsl.Samples.sendTypes]
     *
     */
    public open fun send(
        from: SKRole,
        to: SKRole,
        type: Class<*>,
        label: String? = null,
        condition: String = "",
    ) {
        if (label != null) {
            if (label in msgLabels) {
                throw DuplicateMessageLabelException(label)
            } else {
                msgLabels.add(label)
            }
        }
        // TODO
        if (condition.isNotBlank()) {
            RefinementParser.parseToEnd(condition)
        }

        val msg = Send(from, to, type, label, condition)
        roles.add(from)
        roles.add(to)

        instructions.add(msg)
    }

    /**
     *
     * Declares an internal choice at [at].
     *
     * @param [at] role that makes the decision
     * @param [branches] block that defines the choices
     *
     *
     * @sample [org.david.sessionkotlin.dsl.Samples.choice]
     *
     */
    public open fun choice(at: SKRole, branches: ChoiceEnv.() -> Unit) {
        val bEnv = ChoiceEnv(roles, recursionVariables)
        bEnv.branches()
        val b = Choice(at, bEnv.branchMap)

        roles.add(at)
        for (g in b.branchMap.values) {
            roles.addAll(g.roles)
            for (l in g.msgLabels) {
                if (l in msgLabels) {
                    throw DuplicateMessageLabelException(l)
                } else {
                    msgLabels.add(l)
                }
            }
        }
        instructions.add(b)
    }

    /**
     *
     * Inlines a global protocol.
     *
     * @param [protocolBuilder] protocol to inline
     * @param [roleMapper] optional map to replace roles in the protocol
     *
     * @sample [org.david.sessionkotlin.dsl.Samples.exec]
     *
     */
    public open fun exec(protocolBuilder: GlobalEnv, roleMapper: Map<SKRole, SKRole> = emptyMap()) {
        // We must merge the protocols
        val cleanMap = roleMapper.filterKeys { protocolBuilder.roles.contains(it) }
        instructions.addAll(protocolBuilder.instructions.map { it.mapped(cleanMap) })
        roles.addAll(protocolBuilder.roles.map { cleanMap.getOrKey(it) })
        recursionVariables.addAll(protocolBuilder.recursionVariables)

        for (l in protocolBuilder.msgLabels) {
            if (l in msgLabels) {
                throw DuplicateMessageLabelException(l)
            } else {
                msgLabels.add(l)
            }
        }
    }

    /**
     *
     * Recursion definition.
     *
     * @param label unique recursion label (optional)
     *
     * @sample [org.david.sessionkotlin.dsl.Samples.goto]
     *
     * @return a [RecursionTag] to be used in [goto] calls.
     *
     */
    public open fun miu(): RecursionTag {
        val tag = RecursionTag()
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
     * @sample [org.david.sessionkotlin.dsl.Samples.goto]
     *
     */
    public open fun goto(tag: RecursionTag) {
        if (!recursionVariables.contains(tag)) {
            throw UndefinedRecursionVariableException(tag)
        }
        val msg = Recursion(tag)
        instructions.add(msg)
    }

    /**
     * Prints the protocol to the standard output.
     */
    public fun dump(indent: Int = 0) {
        printlnIndent(indent, "{")
        for (i in instructions) {
            i.dump(indent)
        }
        printlnIndent(indent, "}")
    }

    internal fun project(role: SKRole): LocalType {
        if (!roles.contains(role)) {
            throw ProjectionTargetException(role)
        }
        val state = ProjectionState(role)
        return asGlobalType()
            .project(role, state)
            .let {
                println("" + role + " " + state.unguardedRecursions)
                it
            }
            .removeRecursions(state.unguardedRecursions)
    }

    internal fun validate() {
        roles.forEach {
            try {
                project(it)
            } catch (e: SessionKotlinDSLException) {
                System.err.println("Exception while projecting $it:")
                throw e
            }
        }
    }

    internal fun asGlobalType() = buildGlobalType(instructions)
}

/**
 * Recursively builds a [GlobalType] from a list of [Instruction].
 */
private fun buildGlobalType(
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
            is Send -> GlobalTypeSend(
                head.from,
                head.to,
                head.type,
                head.msgLabel,
                head.condition,
                buildGlobalType(tail)
            )
            is Choice -> GlobalTypeChoice(head.at, head.branchMap.mapValues { buildGlobalType(it.value.instructions) })
            is Recursion -> GlobalTypeRecursion(head.tag)
            is RecursionDefinition -> GlobalTypeRecursionDefinition(head.tag, buildGlobalType(tail))
        }
    }

/**
 * Base environment
 */
internal class RootEnv(
    internal val protocolName: String,
) : GlobalEnv(emptySet(), emptySet())

/**
 * Environment for choice branches
 */
internal class NonRootEnv(
    roles: Set<SKRole>,
    recursionVariables: Set<RecursionTag>,
) : GlobalEnv(roles, recursionVariables)

/**
 * Global protocol builder.
 */
internal fun globalProtocolInternal(name: String = "Proto", protocolBuilder: GlobalEnv.() -> Unit): RootEnv {
    val p = RootEnv(name)
    p.protocolBuilder()
    p.validate()
    return p
}

/**
 * Global protocol builder.
 *
 * Generates local APIs.
 */
public fun globalProtocol(name: String, callbacks: Boolean = false, protocolBuilder: GlobalEnv.() -> Unit) {
    generateAPI(globalProtocolInternal(name, protocolBuilder), callbacks)
}
