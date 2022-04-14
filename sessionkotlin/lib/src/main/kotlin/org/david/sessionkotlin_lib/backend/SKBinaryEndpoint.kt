package org.david.sessionkotlin_lib.backend

import java.io.Closeable

internal interface SKBinaryEndpoint : Closeable {
    suspend fun readMsg(): SKMessage
    suspend fun writeMsg(msg: SKMessage)
}
