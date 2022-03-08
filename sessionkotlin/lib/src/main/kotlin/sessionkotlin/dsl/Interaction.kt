package sessionkotlin.dsl


open class Interaction(internal val initiator: Role) {
    open fun dump() {
        throw NotImplementedError()
    }

}

class Send<T>(
    private val from: Role,
    private val to: Role
): Interaction(from) {
    override fun dump() {
        println("Send[$from -> $to]")
    }
}

class Branch(
    private val at: Role,
    private val cases: MutableMap<String, GlobalEnv>
): Interaction(at) {

    init {
        for (c in cases) {
            val interactions = c.value.interactions
            if (interactions.isNotEmpty()) {
                val caseInitiator = interactions.first().initiator
                if (caseInitiator != at) {
                    throw RoleInCaseNotEnabledException(c.key, caseInitiator)
                }
            }

        }

    }

    override fun dump() {
        println("Branch[$at, cases: ")

        for (c in cases) {
            println("${c.key}:")
            c.value.debug()
        }

        println("]")
    }
}