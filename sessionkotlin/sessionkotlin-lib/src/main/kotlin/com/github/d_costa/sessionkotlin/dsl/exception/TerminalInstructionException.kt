package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.TerminalInstruction

/**
 * Thrown when attempting to continue the protocol after a terminal instruction.
 */
public class TerminalInstructionException internal constructor(i: TerminalInstruction) :
    SessionKotlinDSLException("${i.simpleName()} is a terminal operation.")
