package org.david.sessionkotlin_lib.backend.exception

import org.david.sessionkotlin_lib.dsl.SKRole

public open class BinaryEndpointsException(asked: SKRole, vararg between: SKRole) :
    SessionKotlinBackendException("Cannot get channels for role $asked. SKChannel is between (${between.joinToString(", ")})")
