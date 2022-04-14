package org.david.sessionkotlin_lib.backend.exception

import org.david.sessionkotlin_lib.api.SKGenRole

public class AlreadyConnectedException(role: SKGenRole) : SessionKotlinBackendException("Already connected to $role")
