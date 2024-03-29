package com.github.sessionkotlin.lib.dsl

import com.github.sessionkotlin.lib.api.FluentAPIGenerator
import com.github.sessionkotlin.lib.backend.message.SKMessage
import com.github.sessionkotlin.lib.dsl.exception.*
import com.github.sessionkotlin.lib.dsl.types.*
import com.github.sessionkotlin.lib.fsm.statesFromLocalType
import com.github.sessionkotlin.lib.util.hasWhitespace
import com.github.sessionkotlin.lib.util.printlnIndent
import com.github.sessionkotlin.parser.RefinementCondition
import com.github.sessionkotlin.parser.RefinementParser
import mu.KotlinLogging
import org.sosy_lab.common.ShutdownManager
import org.sosy_lab.common.configuration.Configuration
import org.sosy_lab.common.log.BasicLogManager
import org.sosy_lab.common.log.LogManager
import org.sosy_lab.java_smt.SolverContextFactory
import org.sosy_lab.java_smt.SolverContextFactory.Solvers
import java.io.File

/**
 * Alias of a function type with [GlobalEnv] as receiver.
 */
public typealias GlobalProtocol = GlobalEnv.() -> Unit
private val logger = KotlinLogging.logger {}

internal data class MsgExchange(private val action: Action, val label: String, val a: SKRole, val b: SKRole) {
    internal enum class Action {
        Send, Receive
    }

    override fun toString(): String {
        return "$action($a, $b, $label)"
    }
}

@SessionKotlinDSL
public sealed class GlobalEnv(
    roles: Set<SKRole>,
    recursionVariables: Set<RecursionTag>,
) {
    internal var instructions = mutableListOf<Instruction>()
    internal val roles = roles.toMutableSet()
    private val recursionVariables = recursionVariables.toMutableSet()
    internal val msgLabels = mutableListOf<MsgExchange>()

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
     * @sample [com.github.sessionkotlin.lib.dsl.Samples.send]
     *
     */
    public inline fun <reified T> send(
        from: SKRole,
        to: SKRole,
        label: String = SKMessage.DEFAULT_LABEL,
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
     * @sample [com.github.sessionkotlin.lib.dsl.Samples.sendTypes]
     *
     */
    public open fun send(
        from: SKRole,
        to: SKRole,
        type: Class<*>,
        label: String = SKMessage.DEFAULT_LABEL,
        condition: String = "",
    ) {
        msgLabels.add(MsgExchange(MsgExchange.Action.Send, label, from, to))
        msgLabels.add(MsgExchange(MsgExchange.Action.Receive, label, from, to))

        var refinementCondition: RefinementCondition? = null

        if (condition.isNotBlank()) {
            refinementCondition = RefinementCondition(condition, RefinementParser.parseToEnd(condition))
        }

        if (label.hasWhitespace()) {
            throw BranchLabelWhitespaceException(label)
        }

        val msg = Send(from, to, type, label, refinementCondition)
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
     * @sample [com.github.sessionkotlin.lib.dsl.Samples.choice]
     *
     */
    public open fun choice(at: SKRole, branches: ChoiceEnv.() -> Unit) {
        val bEnv = ChoiceEnv(roles, recursionVariables)
        bEnv.branches()
        val b = Choice(at, bEnv.branchMap)

        roles.add(at)
        for (g in b.branches) {
            roles.addAll(g.roles)
            msgLabels.addAll(g.msgLabels)
        }
        instructions.add(b)
    }

    /**
     *
     * Recursion definition.
     *
     * @sample [com.github.sessionkotlin.lib.dsl.Samples.goto]
     *
     * @return a [RecursionTag] to be used in [goto] calls.
     *
     */
    public open fun mu(): RecursionTag {
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
     * @param [tag] the tag of the recursion point to return to.
     *
     * @sample [com.github.sessionkotlin.lib.dsl.Samples.goto]
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
    internal fun dump(indent: Int = 0) {
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

    private fun logError(role: SKRole) = logger.error { "Exception while projecting onto $role" }

    internal fun validate() {
        val g = asGlobalType()
        val localTypes = roles.map {
            try {
                val state = ProjectionState(it)
                Pair(it, g.project(it, state).removeRecursions(state.emptyRecursions))
            } catch (e: SessionKotlinDSLException) {
                logError(it)
                throw e
            }
        }
        localTypes.forEach { (role, localType) ->
            try {
                statesFromLocalType(localType) // check determinism
            } catch (e: SessionKotlinDSLException) {
                logError(role)
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

        val unsatExpressions = g.getUnsat(satState)
        if (unsatExpressions.isNotEmpty()) {
            throw UnsatisfiableRefinementsException(unsatExpressions)
        }
    }

    private fun asGlobalType() = buildGlobalType(instructions)
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
            is Choice -> GlobalTypeChoice(head.at, head.branches.map { buildGlobalType(it.instructions) })
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
    val outputDirectory = File("build/generated/sessionkotlin/main/kotlin")

    val g = globalProtocolInternal(name, protocolBuilder)
    if (callbacks) {
        val dupeMsgLabels = g.msgLabels.groupingBy { it }.eachCount().filter { it.value > 1 }
        if (dupeMsgLabels.isNotEmpty()) {
            throw DuplicateMessageLabelsException(dupeMsgLabels.keys.map { it.toString() })
        }
        com.github.sessionkotlin.lib.api.CallbacksAPIGenerator(g).writeTo(outputDirectory)
    }
    FluentAPIGenerator(g).writeTo(outputDirectory)
}
