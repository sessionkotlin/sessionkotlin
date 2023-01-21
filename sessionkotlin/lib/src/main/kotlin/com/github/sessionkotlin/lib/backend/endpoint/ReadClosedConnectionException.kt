package com.github.sessionkotlin.lib.backend.endpoint

import com.github.sessionkotlin.lib.api.SKGenRole
import com.github.sessionkotlin.lib.backend.SessionKotlinBackendException

/**
 * Thrown when attempting to read from a closed connection.
 */
public class ReadClosedConnectionException(role: SKGenRole) : SessionKotlinBackendException("Could not receive a message from $role. Make sure that $role executes the whole protocol.")
