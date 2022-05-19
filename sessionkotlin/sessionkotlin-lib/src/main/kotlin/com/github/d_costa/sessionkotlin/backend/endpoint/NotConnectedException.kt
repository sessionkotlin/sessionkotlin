package com.github.d_costa.sessionkotlin.backend.endpoint

import com.github.d_costa.sessionkotlin.api.SKGenRole
import com.github.d_costa.sessionkotlin.backend.SessionKotlinBackendException

/**
 * Thrown when trying to communicate with an endpoint that is not connected.
 */
public class NotConnectedException(role: SKGenRole) : SessionKotlinBackendException("Not connected to $role")
