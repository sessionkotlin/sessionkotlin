package dsl.exception

import dsl.Role

class InconsistentExternalChoiceException(role: Role, activators: Collection<Role>) :
    SessionKotlinException("Inconsistent external choice: role $role activated by [${activators.joinToString()}]")
