package org.david.sessionkotlin_lib.dsl.types

import org.david.sessionkotlin_lib.dsl.Role

internal sealed class LocalType
internal class LocalTypeSend(val to: Role, val type: Class<*>, val cont: LocalType) : LocalType()
internal class LocalTypeReceive(val from: Role, val type: Class<*>, val cont: LocalType) : LocalType()
internal class LocalTypeInternalChoice(val cases: Map<String, LocalType>) : LocalType()
internal class LocalTypeExternalChoice(val to: Role, val cases: Map<String, LocalType>) : LocalType()
internal object LocalTypeEnd : LocalType()
internal object LocalTypeRec : LocalType()
internal object LocalTypeNOP : LocalType()


internal fun LocalType.asString(): String =
    when (this) {
        is LocalTypeSend -> "!$to <${type.simpleName}> . ${cont.asString()}"
        is LocalTypeReceive -> "?$from <${type.simpleName}> . ${cont.asString()}"
        is LocalTypeExternalChoice -> "&$to ${cases.map { (k, v) -> "$k: ${v.asString()}" }}"
        is LocalTypeInternalChoice -> "+ ${cases.map { (k, v) -> "$k: ${v.asString()}" }}"
        LocalTypeNOP -> ""
        LocalTypeRec -> "rec"
        LocalTypeEnd -> "end"

    }