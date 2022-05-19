package com.github.d_costa.sessionkotlin.backend.socket

import com.github.d_costa.sessionkotlin.backend.endpoint.SKConnection
import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.backend.message.SKMessageFormatter
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import java.nio.ByteBuffer

/**
 * Endpoint implementation with sockets.
 */
internal class SKSocketConnection(
    private var s: Socket,
    private val objFormatter: SKMessageFormatter,
) : SKConnection {

    /**
     * As messages are often very small, we must flush after every send or else
     * the endpoint would become stuck waiting for more data to fill the buffer before sending.
     */
    private val outputStream = s.openWriteChannel(autoFlush = true)
    private val inputStream = s.openReadChannel()

    override fun close() {
        s.close()
    }

    override suspend fun readMsg(): SKMessage {
        val size = inputStream.readInt()
        val b = ByteBuffer.wrap(ByteArray(size))
        inputStream.readFully(b)
        return objFormatter.fromBytes(b.array())
    }

    override suspend fun writeMsg(msg: SKMessage) {
        val msgBytes = objFormatter.toBytes(msg)
        outputStream.writeInt(msgBytes.size)
        outputStream.writeFully(msgBytes)
    }
}
