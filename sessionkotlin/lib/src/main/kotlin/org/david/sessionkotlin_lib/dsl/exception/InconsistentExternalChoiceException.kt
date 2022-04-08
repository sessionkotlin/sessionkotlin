package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.SKRole

public class InconsistentExternalChoiceException(role: SKRole, activators: Collection<SKRole>) :
    SessionKotlinDSLException("Inconsistent external choice: role $role activated by [${activators.joinToString()}]")
