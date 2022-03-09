package sessionkotlin.dsl


abstract class Interaction(
    internal val initiator: Role,
    internal val target: Role?,
) {
    abstract fun dump()
}

class Send<T>(
    private val from: Role,
    private val to: Role,
) : Interaction(from, to) {

    override fun dump() {
        println("Send[$from -> $to]")
    }

}

class Branch(
    private val at: Role,
    private val caseMap: MutableMap<String, GlobalEnv>,
) : Interaction(at, null) {

    init {
        for ((_, cases) in caseMap) {
            val activatedRoles = mutableSetOf(at)

            for (interaction in cases.interactions) {
                if (interaction.initiator in activatedRoles) {
                    if (interaction.target != null) {
                        activatedRoles.add(interaction.target)
                    }
                } else {
                    throw RoleNotEnabledException(interaction.initiator)
                }
            }
        }

    }

    override fun dump() {
        println("Branch[$at, cases: ")

        for (c in caseMap) {
            println("${c.key}:")
            c.value.debug()
        }

        println("]")
    }
}