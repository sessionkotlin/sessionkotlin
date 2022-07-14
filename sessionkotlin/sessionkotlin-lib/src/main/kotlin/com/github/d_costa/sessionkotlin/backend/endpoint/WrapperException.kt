package com.github.d_costa.sessionkotlin.backend.endpoint

import com.github.d_costa.sessionkotlin.backend.SessionKotlinBackendException

/**
 * Thrown when trying to wrap a non-socket connection.
 */
public class WrapperException : SessionKotlinBackendException("Only socket connections can be wrapped.")
