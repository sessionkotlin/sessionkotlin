package org.david.sessionkotlin_lib.backend.exception

import org.david.sessionkotlin_lib.dsl.SKRole

public class NotConnectedException(role: SKRole) : SessionKotlinBackendException("Not connected to $role")
