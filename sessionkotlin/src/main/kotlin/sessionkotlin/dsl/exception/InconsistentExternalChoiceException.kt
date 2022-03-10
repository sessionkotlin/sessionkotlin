package sessionkotlin.dsl.exception

import sessionkotlin.dsl.Role

class InconsistentExternalChoiceException(role: Role, activators: Collection<Role>):
    SessionKotlinException("Inconsistent external choice: role $role activated by [${activators.joinToString()}]")
