package com.github.d_costa.sessionkotlin.backend.endpoint

import com.github.d_costa.sessionkotlin.api.SKGenRole
import com.github.d_costa.sessionkotlin.backend.SessionKotlinBackendException

public class AlreadyConnectedException(role: SKGenRole) : SessionKotlinBackendException("Already connected to $role")
