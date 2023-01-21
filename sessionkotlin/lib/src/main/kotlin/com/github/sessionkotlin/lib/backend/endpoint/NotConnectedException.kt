package com.github.sessionkotlin.lib.backend.endpoint

import com.github.sessionkotlin.lib.api.SKGenRole
import com.github.sessionkotlin.lib.backend.SessionKotlinBackendException

/**
 * Thrown when trying to communicate with an endpoint that is not connected.
 */
public class NotConnectedException(role: SKGenRole) : SessionKotlinBackendException("Not connected to $role")
