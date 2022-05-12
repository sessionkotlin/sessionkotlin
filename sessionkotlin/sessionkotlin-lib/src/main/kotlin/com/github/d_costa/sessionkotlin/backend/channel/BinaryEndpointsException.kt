package com.github.d_costa.sessionkotlin.backend.channel

import com.github.d_costa.sessionkotlin.api.SKGenRole
import com.github.d_costa.sessionkotlin.backend.SessionKotlinBackendException

public class BinaryEndpointsException(asked: SKGenRole) :
    SessionKotlinBackendException("Cannot get channel endpoints for role $asked")
