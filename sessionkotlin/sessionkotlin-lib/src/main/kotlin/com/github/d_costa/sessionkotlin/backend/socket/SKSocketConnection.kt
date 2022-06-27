package com.github.d_costa.sessionkotlin.backend.socket

import com.github.d_costa.sessionkotlin.backend.endpoint.SKConnection
import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.backend.message.SKMessageFormatter
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.yield
import java.nio.ByteBuffer
import java.util.*

/**
 * Endpoint implementation with sockets.
 */
internal class SKSocketConnection(
    private var s: Socket,
    private val objFormatter: SKMessageFormatter,
) : SKConnection {
    private val bufferSize = 16_384

    /**
     * As messages are often very small, we must flush after every send or else
     * the endpoint would become stuck waiting for more data to fill the buffer before sending.
     */
    private val outputStream = s.openWriteChannel(autoFlush = true)
    private val inputStream = s.openReadChannel()
    private val buffer = ByteBuffer.allocate(bufferSize)

    override fun close() {
        s.close()
    }

    override suspend fun readMsg(): SKMessage {
        inputStream.read {
            buffer.put(it)
            buffer.flip()
        }
        val o = objFormatter.fromBytes(buffer)
        return if (o.isPresent) o.get() else readMsg()
    }

    override suspend fun writeMsg(msg: SKMessage) {
        val msgBytes = objFormatter.toBytes(msg)
        outputStream.writeFully(msgBytes)
    }
}
