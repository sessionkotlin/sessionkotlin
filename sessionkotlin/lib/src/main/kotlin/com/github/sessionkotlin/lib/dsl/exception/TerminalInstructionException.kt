package com.github.sessionkotlin.lib.dsl.exception

import com.github.sessionkotlin.lib.dsl.TerminalInstruction

/**
 * Thrown when attempting to continue the protocol after a terminal instruction.
 */
internal class TerminalInstructionException internal constructor(i: TerminalInstruction) :
    SessionKotlinDSLException("${i.simpleName()} is a terminal operation.")
