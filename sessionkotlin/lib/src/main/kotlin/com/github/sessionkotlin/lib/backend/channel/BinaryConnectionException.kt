package com.github.sessionkotlin.lib.backend.channel

import com.github.sessionkotlin.lib.api.SKGenRole
import com.github.sessionkotlin.lib.backend.SessionKotlinBackendException

/**
 * Thrown when there is a problem obtaining the endpoints for the channel.
 */
public class BinaryConnectionException(askedRole: SKGenRole) :
    SessionKotlinBackendException("Cannot get channel connection for role $askedRole")
