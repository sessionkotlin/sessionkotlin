package com.github.d_costa.sessionkotlin.backend.exception

import com.github.d_costa.sessionkotlin.api.SKGenRole

public class BinaryEndpointsException(asked: SKGenRole) :
    SessionKotlinBackendException("Cannot get channel endpoints for role $asked")
