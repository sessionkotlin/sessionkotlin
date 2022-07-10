package com.github.d_costa.sessionkotlin.backend.endpoint

import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import java.io.Closeable

/**
 * An endpoint with read and write operations.
 */
internal interface MessageIO : Closeable {
    /**
     * @throws [kotlinx.coroutines.channels.ClosedReceiveChannelException] when closed to receive.
     */
    suspend fun readMsg(): SKMessage
    suspend fun writeMsg(msg: SKMessage)
}
