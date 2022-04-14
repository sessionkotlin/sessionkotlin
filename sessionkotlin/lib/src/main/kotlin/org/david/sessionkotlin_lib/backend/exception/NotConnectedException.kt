package org.david.sessionkotlin_lib.backend.exception

import org.david.sessionkotlin_lib.api.SKGenRole

public class NotConnectedException(role: SKGenRole) : SessionKotlinBackendException("Not connected to $role")
