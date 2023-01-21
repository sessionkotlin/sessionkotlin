package com.github.sessionkotlin.lib.backend.endpoint

import com.github.sessionkotlin.lib.backend.SessionKotlinBackendException

/**
 * Thrown when trying to wrap a non-socket connection.
 */
public class WrapperException : SessionKotlinBackendException("Only socket connections can be wrapped.")
