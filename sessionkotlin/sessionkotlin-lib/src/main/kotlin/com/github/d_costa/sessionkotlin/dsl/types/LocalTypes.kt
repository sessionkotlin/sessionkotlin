package com.github.d_costa.sessionkotlin.dsl.types

import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole

internal sealed class LocalType {
    /**
     * Replaces [LocalTypeRecursion] with [LocalTypeEnd] and
     * [LocalTypeRecursionDefinition] with [LocalTypeRecursionDefinition.cont].
     */
    abstract fun removeRecursions(tags: Set<RecursionTag>): LocalType
}
internal data class MsgLabel(val label: String = "", val mentioned: Boolean = false)

internal data class LocalTypeSend(
    val to: SKRole,
    val type: Class<*>,
    val msgLabel: MsgLabel,
    val condition: String,
    val cont: LocalType
) : LocalType() {
    constructor(to: SKRole, type: Class<*>, cont: LocalType) : this(to, type, MsgLabel(), "", cont)
    constructor(to: SKRole, type: Class<*>, msgLabel: MsgLabel, cont: LocalType) : this(to, type, msgLabel, "", cont)
    constructor(to: SKRole, type: Class<*>, condition: String, cont: LocalType) : this(to, type, MsgLabel(), condition, cont)

    override fun removeRecursions(tags: Set<RecursionTag>) =
        LocalTypeSend(to, type, msgLabel, condition, cont.removeRecursions(tags))

    override fun equals(other: Any?): Boolean {
        if (other !is LocalTypeSend) return false
        return to == other.to &&
            type == other.type &&
            cont == other.cont &&
            msgLabel == other.msgLabel &&
            condition == other.condition
    }

    override fun hashCode(): Int {
        var result = to.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + cont.hashCode()
        result = 31 * result + msgLabel.hashCode()
        result = 31 * result + condition.hashCode()
        return result
    }
}

internal data class LocalTypeReceive(
    val from: SKRole,
    val type: Class<*>,
    val msgLabel: MsgLabel,
    val cont: LocalType,
) : LocalType() {
    constructor(to: SKRole, type: Class<*>, cont: LocalType) : this(to, type, MsgLabel(), cont)

    override fun removeRecursions(tags: Set<RecursionTag>) =
        LocalTypeReceive(from, type, msgLabel, cont.removeRecursions(tags))
}

internal data class LocalTypeInternalChoice(val branches: List<LocalType>) : LocalType() {
    override fun removeRecursions(tags: Set<RecursionTag>) =
        LocalTypeInternalChoice(branches.map { it.removeRecursions(tags) })
}

internal data class LocalTypeExternalChoice(var of: SKRole, val branches: List<LocalType>) : LocalType() {
    override fun removeRecursions(tags: Set<RecursionTag>) =
        LocalTypeExternalChoice(of, branches.map { it.removeRecursions(tags) })
}

internal data class LocalTypeRecursionDefinition(val tag: RecursionTag, val cont: LocalType) : LocalType() {
    override fun removeRecursions(tags: Set<RecursionTag>) =
        if (tag in tags)
            cont.removeRecursions(tags)
        else
            LocalTypeRecursionDefinition(tag, cont.removeRecursions(tags))
}

internal data class LocalTypeRecursion(val tag: RecursionTag) : LocalType() {
    override fun removeRecursions(tags: Set<RecursionTag>) =
        if (tag in tags)
            LocalTypeEnd
        else
            LocalTypeRecursion(tag)
}

internal object LocalTypeEnd : LocalType() {
    override fun removeRecursions(tags: Set<RecursionTag>) = LocalTypeEnd
}
internal typealias LEnd = LocalTypeEnd

internal fun LocalType.containsTag(tag: RecursionTag): Boolean =
    when (this) {
        is LocalTypeSend -> cont.containsTag(tag)
        is LocalTypeReceive -> cont.containsTag(tag)
        is LocalTypeExternalChoice -> branches.any { it.containsTag(tag) }
        is LocalTypeInternalChoice -> branches.any { it.containsTag(tag) }
        is LocalTypeRecursion -> this.tag == tag
        is LocalTypeRecursionDefinition -> cont.containsTag(tag)
        LocalTypeEnd -> false
    }

private fun tabs(i: Int) = "\t".repeat(i)

internal fun LocalType.asString(): String = asString(0)

internal fun LocalType.asString(i: Int = 0): String =
    when (this) {
        is LocalTypeSend -> "$to!<${type.simpleName}> . ${cont.asString(i)}"
        is LocalTypeReceive -> "$from?<${type.simpleName}> . ${cont.asString(i)}"
        is LocalTypeExternalChoice -> "\n${tabs(i)}&$of \n${tabs(i)}${
        branches.joinToString("\n${tabs(i)}") { it.asString(i + 1) }
        }"
        is LocalTypeInternalChoice -> "\n${tabs(i)}+\n${tabs(i)}${
        branches.joinToString("\n${tabs(i)}") { it.asString(i + 1) }
        }"
        is LocalTypeRecursion -> "$tag"
        is LocalTypeRecursionDefinition -> "mu_$tag . ${cont.asString(i)}"
        LocalTypeEnd -> "end"
    }
