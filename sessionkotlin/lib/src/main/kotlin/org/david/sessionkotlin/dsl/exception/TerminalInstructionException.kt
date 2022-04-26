package org.david.sessionkotlin.dsl.exception

import org.david.sessionkotlin.dsl.TerminalInstruction

public class TerminalInstructionException internal constructor(i: TerminalInstruction) :
    SessionKotlinDSLException("${i.simpleName()} is a terminal operation.")
