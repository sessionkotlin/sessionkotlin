package com.github.d_costa.sessionkotlin.dsl

import com.github.d_costa.sessionkotlin.api.generateAPI
import com.github.d_costa.sessionkotlin.dsl.exception.*
import com.github.d_costa.sessionkotlin.dsl.types.*
import com.github.d_costa.sessionkotlin.parser.RefinementParser
import com.github.d_costa.sessionkotlin.util.printlnIndent
import org.sosy_lab.common.ShutdownManager
import org.sosy_lab.common.configuration.Configuration
import org.sosy_lab.common.log.BasicLogManager
import org.sosy_lab.common.log.LogManager
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.SolverContextFactory.Solvers

/**
 * Alias of a function type with [GlobalEnv] as receiver.
 */
public typealias GlobalProtocol = GlobalEnv.() -> Unit

@SessionKotlinDSL
public sealed class GlobalEnv(
    roles: Set<SKRole>,
    recursionVariables: Set<RecursionTag>,
) {
    internal var instructions = mutableListOf<Instruction>()
    internal val roles = roles.toMutableSet()
    internal val recursionVariables = recursionVariables.toMutableSet()
    private val msgLabels = mutableSetOf<String>()

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
     * @param [label] optional, but unique, message label. Required if using callbacks API.
     * @param [condition] optional refinement expression
     *
     * @sample [com.github.d_costa.sessionkotlin.dsl.Samples.send]
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
     * @param [type] message type
     * @param [label] optional, but unique, message label. Required if using callbacks API.
     * @param [condition] optional refinement expression
     *
     * @sample [com.github.d_costa.sessionkotlin.dsl.Samples.sendTypes]
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
     * @param [branches] block that defines choice branches
     *
     *
     * @sample [com.github.d_costa.sessionkotlin.dsl.Samples.choice]
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
     * Recursion definition.
     *
     * @sample [com.github.d_costa.sessionkotlin.dsl.Samples.goto]
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
     * @sample [com.github.d_costa.sessionkotlin.dsl.Samples.goto]
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
            .removeRecursions(state.emptyRecursions)
    }

    internal fun validate() {
        val g = asGlobalType()
        roles.forEach {
            try {
                g.project(it)
            } catch (e: SessionKotlinDSLException) {
                System.err.println("Exception while projecting $it:")
                throw e
            }
        }
        val config: Configuration = Configuration.defaultConfiguration()
        val logger: LogManager = BasicLogManager.create(config)
        val shutdown = ShutdownManager.create()
        val context = SolverContextFactory.createSolverContext(
            config, logger, shutdown.notifier, Solvers.Z3
        )

        val satState = SatState(context)

        if (!g.sat(satState)) {
            throw UnsatisfiableRefinementsException()
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
 * Global protocol builder. Generates local APIs.
 */
public fun globalProtocol(name: String, callbacks: Boolean = false, protocolBuilder: GlobalEnv.() -> Unit) {
    generateAPI(globalProtocolInternal(name, protocolBuilder), callbacks)
}
