package com.github.d_costa.sessionkotlin.backend.endpoint

import com.github.d_costa.sessionkotlin.api.SKGenRole
import com.github.d_costa.sessionkotlin.backend.SessionKotlinBackendException

/**
 * Thrown when attempting to connect to an endpoint when a connection is already in place.
 */
public class AlreadyConnectedException(role: SKGenRole) : SessionKotlinBackendException("Already connected to $role")
