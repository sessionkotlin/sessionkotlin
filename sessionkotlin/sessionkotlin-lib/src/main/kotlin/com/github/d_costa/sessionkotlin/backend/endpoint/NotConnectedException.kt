package com.github.d_costa.sessionkotlin.backend.endpoint

import com.github.d_costa.sessionkotlin.api.SKGenRole
import com.github.d_costa.sessionkotlin.backend.SessionKotlinBackendException

public class NotConnectedException(role: SKGenRole) : SessionKotlinBackendException("Not connected to $role")
