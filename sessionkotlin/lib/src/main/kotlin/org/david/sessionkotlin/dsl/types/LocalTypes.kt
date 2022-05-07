package org.david.sessionkotlin.dsl.types

import org.david.sessionkotlin.dsl.RecursionTag
import org.david.sessionkotlin.dsl.SKRole

internal sealed class LocalType
internal data class LocalTypeSend(
    val to: SKRole,
    val type: Class<*>,
    val cont: LocalType,
    val branchLabel: String? = null,
    val msgLabel: String? = null,
    val condition: String = "",
) : LocalType() {
    override fun equals(other: Any?): Boolean {
        if (other !is LocalTypeSend) return false
        return to == other.to &&
            type == other.type &&
            cont == other.cont &&
            msgLabel == other.msgLabel &&
            condition == other.condition &&
            ((branchLabel == null && other.branchLabel == null) || (branchLabel != null && other.branchLabel != null))
    }

    override fun hashCode(): Int {
        var result = to.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + cont.hashCode()
        result = 31 * result + (msgLabel?.hashCode() ?: 0)
        result = 31 * result + condition.hashCode()
        return result
    }
}

internal data class LocalTypeReceive(
    val from: SKRole,
    val type: Class<*>,
    val cont: LocalType,
    val msgLabel: String? = null,
) : LocalType()

internal data class LocalTypeInternalChoice(val branches: Map<String, LocalType>) : LocalType()
internal data class LocalTypeExternalChoice(var to: SKRole, val branches: Map<String, LocalType>) : LocalType()
internal data class LocalTypeRecursionDefinition(val tag: RecursionTag, val cont: LocalType) : LocalType()
internal data class LocalTypeRecursion(val tag: RecursionTag) : LocalType()
internal object LocalTypeEnd : LocalType()
internal typealias LEnd = LocalTypeEnd

internal fun LocalType.containsTag(tag: RecursionTag): Boolean =
    when (this) {
        is LocalTypeSend -> cont.containsTag(tag)
        is LocalTypeReceive -> cont.containsTag(tag)
        is LocalTypeExternalChoice -> branches.any { it.value.containsTag(tag) }
        is LocalTypeInternalChoice -> branches.any { it.value.containsTag(tag) }
        is LocalTypeRecursion -> this.tag == tag
        is LocalTypeRecursionDefinition -> cont.containsTag(tag)
        LocalTypeEnd -> false
    }

internal fun LocalType.asString(): String =
    when (this) {
        is LocalTypeSend -> "$to!<${type.simpleName}> . ${cont.asString()}"
        is LocalTypeReceive -> "$from?<${type.simpleName}> . ${cont.asString()}"
        is LocalTypeExternalChoice -> "&$to ${branches.map { (k, v) -> "$k: ${v.asString()}" }}"
        is LocalTypeInternalChoice -> "+ ${branches.map { (k, v) -> "$k: ${v.asString()}" }}"
        is LocalTypeRecursion -> "$tag"
        is LocalTypeRecursionDefinition -> "miu_$tag . ${cont.asString()}"
        LocalTypeEnd -> "end"
    }

private fun tabs(i: Int) = "\t".repeat(i)

internal fun LocalType.asFormattedString(): String = asFormattedString(0)

internal fun LocalType.asFormattedString(i: Int = 0): String =
    when (this) {
        is LocalTypeSend -> "$to!<${type.simpleName}> . ${cont.asFormattedString(i)}"
        is LocalTypeReceive -> "$from?<${type.simpleName}> . ${cont.asFormattedString(i)}"
        is LocalTypeExternalChoice -> "\n${tabs(i)}&$to \n${tabs(i)}${
        branches.map { (k, v) -> "$k: ${v.asFormattedString(i + 1)}" }.joinToString("\n${tabs(i)}")
        }"
        is LocalTypeInternalChoice -> "\n${tabs(i)}+\n${tabs(i)}${
        branches.map { (k, v) -> "$k: ${v.asFormattedString(i + 1)}" }.joinToString("\n${tabs(i)}")
        }"
        is LocalTypeRecursion -> "$tag"
        is LocalTypeRecursionDefinition -> "miu_$tag . ${cont.asFormattedString(i)}"
        LocalTypeEnd -> "end"
    }
