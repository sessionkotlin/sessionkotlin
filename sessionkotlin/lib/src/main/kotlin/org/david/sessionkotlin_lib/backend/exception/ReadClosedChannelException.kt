package org.david.sessionkotlin_lib.backend.exception

import org.david.sessionkotlin_lib.api.SKGenRole

public open class ReadClosedChannelException(role: SKGenRole) :
    SessionKotlinBackendException("Could not receive a message from $role. Make sure that $role executes the whole protocol.")
