package com.github.d_costa.sessionkotlin.backend.exception

import com.github.d_costa.sessionkotlin.api.SKGenRole

public class ReadClosedChannelException(role: SKGenRole) :
    SessionKotlinBackendException("Could not receive a message from $role. Make sure that $role executes the whole protocol.")
