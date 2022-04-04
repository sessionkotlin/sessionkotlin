package org.david.sessionkotlin_lib.dsl.exception

import org.david.sessionkotlin_lib.dsl.Role

public class InconsistentExternalChoiceException(role: Role, activators: Collection<Role>) :
    SessionKotlinDSLException("Inconsistent external choice: role $role activated by [${activators.joinToString()}]")
