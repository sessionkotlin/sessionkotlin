package com.github.d_costa.sessionkotlin.backend.exception

import com.github.d_costa.sessionkotlin.api.SKGenRole

public class AlreadyConnectedException(role: SKGenRole) : SessionKotlinBackendException("Already connected to $role")
