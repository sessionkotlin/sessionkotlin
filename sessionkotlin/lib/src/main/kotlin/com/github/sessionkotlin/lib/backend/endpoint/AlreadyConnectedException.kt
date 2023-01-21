package com.github.sessionkotlin.lib.backend.endpoint

import com.github.sessionkotlin.lib.api.SKGenRole
import com.github.sessionkotlin.lib.backend.SessionKotlinBackendException

/**
 * Thrown when attempting to connect to an endpoint when a connection is already in place.
 */
public class AlreadyConnectedException(role: SKGenRole) : SessionKotlinBackendException("Already connected to $role")
