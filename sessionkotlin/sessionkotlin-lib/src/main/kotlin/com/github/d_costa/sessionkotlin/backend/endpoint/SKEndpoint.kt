package com.github.d_costa.sessionkotlin.backend.endpoint

import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import java.io.Closeable

/**
 * An endpoint with read and write operations.
 */
internal interface SKEndpoint : Closeable {
    suspend fun readMsg(): SKMessage
    suspend fun writeMsg(msg: SKMessage)
}