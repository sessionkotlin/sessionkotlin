package org.david.sessionkotlin.backend.exception

import org.david.sessionkotlin.api.SKGenRole

public open class BinaryEndpointsException(asked: SKGenRole) :
    SessionKotlinBackendException("Cannot get channel endpoints for role $asked")
