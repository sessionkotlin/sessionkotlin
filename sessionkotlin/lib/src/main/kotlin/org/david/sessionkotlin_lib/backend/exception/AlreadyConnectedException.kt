package org.david.sessionkotlin_lib.backend.exception

import org.david.sessionkotlin_lib.dsl.SKRole

public class AlreadyConnectedException(role: SKRole) : SessionKotlinBackendException("Already connected to $role")
