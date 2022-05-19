package com.github.d_costa.sessionkotlin.backend.channel

import com.github.d_costa.sessionkotlin.api.SKGenRole
import com.github.d_costa.sessionkotlin.backend.SessionKotlinBackendException

/**
 * Thrown when there is a problem obtaining the endpoints for the channel.
 */
public class BinaryConnectionException(askedRole: SKGenRole) :
    SessionKotlinBackendException("Cannot get channel connection for role $askedRole")
