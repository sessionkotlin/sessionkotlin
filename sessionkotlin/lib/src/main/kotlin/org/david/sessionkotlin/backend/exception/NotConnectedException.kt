package org.david.sessionkotlin.backend.exception

import org.david.sessionkotlin.api.SKGenRole

public class NotConnectedException(role: SKGenRole) : SessionKotlinBackendException("Not connected to $role")
