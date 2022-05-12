package com.github.d_costa.sessionkotlin.backend.endpoint

import com.github.d_costa.sessionkotlin.api.SKGenRole
import com.github.d_costa.sessionkotlin.backend.SessionKotlinBackendException

public class ReadClosedChannelException(role: SKGenRole) :
    SessionKotlinBackendException("Could not receive a message from $role. Make sure that $role executes the whole protocol.")
