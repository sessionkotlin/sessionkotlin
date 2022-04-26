package org.david.sessionkotlin.backend.exception

import org.david.sessionkotlin.api.SKGenRole

public class AlreadyConnectedException(role: SKGenRole) : SessionKotlinBackendException("Already connected to $role")
