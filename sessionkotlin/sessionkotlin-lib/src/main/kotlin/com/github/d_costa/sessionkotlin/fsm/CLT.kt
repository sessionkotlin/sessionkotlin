package com.github.d_costa.sessionkotlin.fsm

import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.types.*

internal fun LocalType.toCLT(tag: RecursionTag? = null): CLT =
    when (this) {
        is LocalTypeSend -> CLTSend(to, type, msgLabel, condition, tag, cont.toCLT())
        is LocalTypeReceive -> CLTReceive(from, type, msgLabel, tag, cont.toCLT())
        is LocalTypeExternalChoice -> CLTExternalChoice(of, tag, branches.map { it.toCLT() })
        is LocalTypeInternalChoice -> CLTInternalChoice(tag, branches.map { it.toCLT() })
        is LocalTypeRecursion -> CLTRecursion(this.tag)
        is LocalTypeRecursionDefinition -> cont.toCLT(this.tag)
        LocalTypeEnd -> CLTEnd
    }

/**
 * Local types with recursion definitions embedded in the other types
 */
internal sealed interface CLT
internal abstract class CLTTagged(val recursionTag: RecursionTag?)

internal class CLTSend(
    val to: SKRole,
    val type: Class<*>,
    val msgLabel: MsgLabel,
    val condition: String,
    val tag: RecursionTag?,
    val cont: CLT,
) : CLT, CLTTagged(tag)

internal class CLTReceive(
    val from: SKRole,
    val type: Class<*>,
    val msgLabel: MsgLabel,
    val tag: RecursionTag?,
    val cont: CLT,
) : CLT, CLTTagged(tag)

internal class CLTExternalChoice(
    val of: SKRole,
    val tag: RecursionTag?,
    val branches: List<CLT>,
) : CLT, CLTTagged(tag)

internal class CLTInternalChoice(
    val tag: RecursionTag?,
    val branches: List<CLT>,
) : CLT, CLTTagged(tag)

internal class CLTRecursion(
    val tag: RecursionTag,
) : CLT

internal object CLTEnd : CLT
