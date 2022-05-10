package com.github.d_costa.sessionkotlin.dsl.exception

import com.github.d_costa.sessionkotlin.dsl.SKRole

public class InconsistentExternalChoiceException(role: SKRole, activators: Collection<SKRole>) :
    SessionKotlinDSLException("Inconsistent external choice: role $role activated by [${activators.joinToString()}]")
