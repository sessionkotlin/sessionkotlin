package com.github.d_costa.sessionkotlin.backend.exception

import com.github.d_costa.sessionkotlin.api.SKGenRole

public class NotConnectedException(role: SKGenRole) : SessionKotlinBackendException("Not connected to $role")
