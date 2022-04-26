package org.david.sessionkotlin_lib.backend.exception

import org.david.sessionkotlin_lib.api.SKGenRole

public open class BinaryEndpointsException(asked: SKGenRole) :
    SessionKotlinBackendException("Cannot get channel endpoints for role $asked")
