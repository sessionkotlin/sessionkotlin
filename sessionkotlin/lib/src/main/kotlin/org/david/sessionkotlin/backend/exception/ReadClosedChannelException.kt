package org.david.sessionkotlin.backend.exception

import org.david.sessionkotlin.api.SKGenRole

public class ReadClosedChannelException(role: SKGenRole) :
    SessionKotlinBackendException("Could not receive a message from $role. Make sure that $role executes the whole protocol.")
