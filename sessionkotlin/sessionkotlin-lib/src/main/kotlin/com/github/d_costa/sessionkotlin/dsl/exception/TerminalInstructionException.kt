package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.TerminalInstruction

public class TerminalInstructionException internal constructor(i: TerminalInstruction) :
    SessionKotlinDSLException("${i.simpleName()} is a terminal operation.")
