/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package sessionkotlin.dsl

import sessionkotlin.dsl.exception.*

@DslMarker
private annotation class SessionKotlinDSL

@SessionKotlinDSL
abstract class GlobalEnv(
    roles: Set<Role>,
    enabledRoles: Set<Role>,
) {
    private val instructions = mutableListOf<Instruction>()
    internal val roles = roles.toMutableSet()
    internal val enabledRoles = enabledRoles.toMutableSet()
    private var recursiveCall = false

    /**
     * key = role activated
     * value = role that activatated the key
     */
    internal val activations = mutableMapOf<Role, Role>()

    /**
     *
     * Declares that [from] should send a message of type [T] to [to].
     *
     * @param [from] role that sends the message
     * @param [to] role that receives the message
     *
     * @throws [sessionkotlin.dsl.exception.SendingtoSelfException]
     * if [from] and [to] are the same.
     *
     * @throws [sessionkotlin.dsl.exception.RecursiveProtocolException]
     * if used after a recursive call.
     *
     * @sample [sessionkotlin.dsl.Samples.send]
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

        if (!enabledRoles.contains(to)) {
            activations[to] = from
        }

        enabledRoles.add(to)
        instructions.add(msg)
    }

    /**
     *
     * Declares an internal choice at [at].
     *
     * @param [at] role that makes the decision
     * @param [cases] block that defines the choices
     *
     * @throws [sessionkotlin.dsl.exception.RoleNotEnabledException] if a role that is not enabled initiates an interaction.
     *
     * @throws [sessionkotlin.dsl.exception.RecursiveProtocolException]
     * if used after a recursive call.
     *
     * @sample [sessionkotlin.dsl.Samples.choice]
     *
     */
    open fun choice(at: Role, cases: ChoiceEnv.() -> Unit) {
        if (recursiveCall) {
            throw RecursiveProtocolException()
        }

        enabledRoles.add(at)

        val bEnv = ChoiceEnv(roles, setOf(at))
        bEnv.cases()
        val b = Branch(at, bEnv.caseMap)

        // If a role is enabled in every case:
        bEnv.caseMap.values.forEach { activations.putAll(it.activations) }
        val counters = mutableMapOf<Role, Int>()

        for (globalEnv in bEnv.caseMap.values) {
            for (role in globalEnv.activations.keys)
                counters.merge(role, 1, Int::plus)
        }
        val enabledInEveryCase =
            counters
                .filterValues { it == bEnv.caseMap.size }
                .keys
        enabledRoles.addAll(enabledInEveryCase)


        instructions.add(b)
        roles.add(at)
    }

    /**
     *
     * Appends a global protocol.
     *
     * @param [protocolBuilder] protocol to append
     *
     * @sample [sessionkotlin.dsl.Samples.exec]
     *
     */
    open fun exec(protocolBuilder: GlobalEnv) {
        if (recursiveCall) {
            throw RecursiveProtocolException()
        }

        // We must merge the protocols
        recursiveCall = protocolBuilder.recursiveCall
        enabledRoles.addAll(protocolBuilder.enabledRoles)
        instructions.addAll(protocolBuilder.instructions)

        for ((k, v) in protocolBuilder.activations) {
            activations.putIfAbsent(k, v)
        }
    }

    /**
     *
     * Declares a recursive call.
     *
     * @throws [sessionkotlin.dsl.exception.RecursiveProtocolException]
     * if used after a recursive call.
     *
     * @sample [sessionkotlin.dsl.Samples.rec]
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
        for (i in instructions) {
            i.dump(indent)
        }
    }
}

class RootEnv() : GlobalEnv(emptySet(), emptySet())

class NonRootEnv(
    roles: Set<Role>,
    enabledRoles: Set<Role>,
) : GlobalEnv(roles, enabledRoles) {

    override fun <T> send(from: Role, to: Role) {
        if (from !in enabledRoles) {
            throw RoleNotEnabledException(from)
        }
        super.send<T>(from, to)
    }

    override fun choice(at: Role, cases: ChoiceEnv.() -> Unit) {
        if (at !in enabledRoles) {
            throw RoleNotEnabledException(at)
        }
        super.choice(at, cases)
    }

    override fun exec(protocolBuilder: GlobalEnv) {
        val r = protocolBuilder.roles
            .minus(protocolBuilder.enabledRoles)
            .minus(enabledRoles)
        if (r.isNotEmpty()) {
            throw RoleNotEnabledException(r.first())
        }

        super.exec(protocolBuilder)
    }

    override fun rec() {
        val notActive = roles.minus(enabledRoles)
        if (notActive.isNotEmpty()) {
            throw RoleNotEnabledException(notActive.first())
        }
        super.rec()
    }

}

@SessionKotlinDSL
class ChoiceEnv(
    private val roles: Set<Role>,
    private val enabledRoles: Set<Role>,
) {
    internal val caseMap = mutableMapOf<String, GlobalEnv>()
    private var activations = mutableMapOf<Role, Set<Role>>()

    fun case(label: String, protocolBuilder: GlobalEnv.() -> Unit) {
        val p = NonRootEnv(roles, enabledRoles)
        p.protocolBuilder()

        for ((key, value) in p.activations) {
            activations.merge(key, setOf(value), Set<Role>::plus)

            val actv = activations.getOrDefault(key, emptySet())
            if (actv.size > 1) {
                throw InconsistentExternalChoiceException(key, actv)
            }
        }

        caseMap[label] = p
    }
}

fun globalProtocol(protocolBuilder: GlobalEnv.() -> Unit): GlobalEnv {
    val p = RootEnv()
    p.protocolBuilder()
    return p
}