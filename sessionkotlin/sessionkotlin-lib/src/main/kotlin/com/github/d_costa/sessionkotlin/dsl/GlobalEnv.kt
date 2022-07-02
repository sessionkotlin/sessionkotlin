package com.github.d_costa.sessionkotlin.dsl

import com.github.d_costa.sessionkotlin.api.FluentAPIGenerator
import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.dsl.exception.*
import com.github.d_costa.sessionkotlin.dsl.types.*
import com.github.d_costa.sessionkotlin.fsm.fsmFromLocalType
import com.github.d_costa.sessionkotlin.parser.RefinementParser
import com.github.d_costa.sessionkotlin.util.hasWhitespace
import com.github.d_costa.sessionkotlin.util.printlnIndent
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

@SessionKotlinDSL
public sealed class GlobalEnv(
    roles: Set<SKRole>,
    recursionVariables: Set<RecursionTag>,
) {
    internal var instructions = mutableListOf<Instruction>()
    internal val roles = roles.toMutableSet()
    private val recursionVariables = recursionVariables.toMutableSet()
    internal val msgLabels = mutableListOf<String>()

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
     * @sample [com.github.d_costa.sessionkotlin.dsl.Samples.sendTypes]
     *
     */
    public open fun send(
        from: SKRole,
        to: SKRole,
        type: Class<*>,
        label: String = SKMessage.DEFAULT_LABEL,
        condition: String = "",
    ) {
        msgLabels.add(label)
        if (condition.isNotBlank()) {
            RefinementParser.parseToEnd(condition)
        }

        if (label.hasWhitespace()) {
            throw BranchLabelWhitespaceException(label)
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
     * @sample [com.github.d_costa.sessionkotlin.dsl.Samples.goto]
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
                Pair(it, g.project(it))
            } catch (e: SessionKotlinDSLException) {
                logError(it)
                throw e
            }
        }
        localTypes.forEach { (role, localType) ->
            try {
                fsmFromLocalType(localType)  // check determinism
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

        if (!g.sat(satState)) {
            throw UnsatisfiableRefinementsException()
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
    val g = globalProtocolInternal(name, protocolBuilder)
    if (callbacks) {
        val dupeMsgLabels = g.msgLabels.groupingBy { it }.eachCount().filter { it.value > 1 }
        if (dupeMsgLabels.isNotEmpty()) {
            throw DuplicateMessageLabelsException(dupeMsgLabels.keys)
        }
    }
    val outputDirectory = File("build/generated/sessionkotlin/main/kotlin")
    FluentAPIGenerator(g).writeTo(outputDirectory)
}
