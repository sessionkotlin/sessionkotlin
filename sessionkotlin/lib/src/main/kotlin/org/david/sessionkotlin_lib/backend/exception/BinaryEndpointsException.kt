package org.david.sessionkotlin_lib.backend.exception

import org.david.sessionkotlin_lib.api.SKGenRole

public open class BinaryEndpointsException(asked: SKGenRole, vararg between: SKGenRole) :
    SessionKotlinBackendException("Cannot get channels for role $asked. SKChannel is between (${between.joinToString(", ")})")
