package org.david.sessionkotlin_lib.dsl.types

import org.david.sessionkotlin_lib.dsl.RecursionTag
import org.david.sessionkotlin_lib.dsl.Role

internal sealed class LocalType
internal data class LocalTypeSend(val to: Role, val type: Class<*>, val cont: LocalType) : LocalType()
internal data class LocalTypeReceive(val from: Role, val type: Class<*>, val cont: LocalType) : LocalType()
internal data class LocalTypeInternalChoice(val cases: Map<String, LocalType>) : LocalType()
internal data class LocalTypeExternalChoice(var to: Role, val cases: Map<String, LocalType>) : LocalType()
internal data class LocalTypeRecursionDefinition(val tag: RecursionTag, val cont: LocalType) : LocalType()
internal data class LocalTypeRecursion(val tag: RecursionTag) : LocalType()
internal object LocalTypeEnd : LocalType()
internal typealias LEnd = LocalTypeEnd

internal fun LocalType.containsTag(tag: RecursionTag): Boolean =
    when (this) {
        is LocalTypeSend -> cont.containsTag(tag)
        is LocalTypeReceive -> cont.containsTag(tag)
        is LocalTypeExternalChoice -> cases.any { it.value.containsTag(tag) }
        is LocalTypeInternalChoice -> cases.any { it.value.containsTag(tag) }
        is LocalTypeRecursion -> this.tag == tag
        is LocalTypeRecursionDefinition -> cont.containsTag(tag)
        LocalTypeEnd -> false
    }

internal fun LocalType.asString(): String =
    when (this) {
        is LocalTypeSend -> "$to!<${type.simpleName}> . ${cont.asString()}"
        is LocalTypeReceive -> "$from?<${type.simpleName}> . ${cont.asString()}"
        is LocalTypeExternalChoice -> "&$to ${cases.map { (k, v) -> "$k: ${v.asString()}" }}"
        is LocalTypeInternalChoice -> "+ ${cases.map { (k, v) -> "$k: ${v.asString()}" }}"
        is LocalTypeRecursion -> "$tag"
        is LocalTypeRecursionDefinition -> "miu_$tag . ${cont.asString()}"
        LocalTypeEnd -> "end"
    }

private fun aux(i: Int) = "\t".repeat(i)

internal fun LocalType.asFormattedString(): String = asFormattedString(0)

internal fun LocalType.asFormattedString(i: Int = 0): String =
    when (this) {
        is LocalTypeSend -> "$to!<${type.simpleName}> . ${cont.asFormattedString(i)}"
        is LocalTypeReceive -> "$from?<${type.simpleName}> . ${cont.asFormattedString(i)}"
        is LocalTypeExternalChoice -> "\n${aux(i)}&$to \n${aux(i)}${
        cases.map { (k, v) -> "$k: ${v.asFormattedString(i + 1)}" }.joinToString("\n${aux(i)}")}"
        is LocalTypeInternalChoice -> "\n${aux(i)}+\n${aux(i)}${
        cases.map { (k, v) -> "$k: ${v.asFormattedString(i + 1)}" }.joinToString("\n${aux(i)}")}"
        is LocalTypeRecursion -> "$tag"
        is LocalTypeRecursionDefinition -> "miu_$tag . ${cont.asFormattedString(i)}"
        LocalTypeEnd -> "end"
    }
