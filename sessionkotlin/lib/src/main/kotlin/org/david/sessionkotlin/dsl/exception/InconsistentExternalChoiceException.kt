package org.david.sessionkotlin.dsl.exception

import org.david.sessionkotlin.dsl.SKRole

public class InconsistentExternalChoiceException(role: SKRole, activators: Collection<SKRole>) :
    SessionKotlinDSLException("Inconsistent external choice: role $role activated by [${activators.joinToString()}]")
