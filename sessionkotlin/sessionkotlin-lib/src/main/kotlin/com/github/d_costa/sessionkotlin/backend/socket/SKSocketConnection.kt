package com.github.d_costa.sessionkotlin.backend.socket

import com.github.d_costa.sessionkotlin.backend.endpoint.SKConnection
import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.backend.message.SKMessageFormatter
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import mu.KotlinLogging
import java.nio.ByteBuffer

/**
 * Endpoint implementation with sockets.
 */
internal class SKSocketConnection(
    private var s: Socket,
    private val objFormatter: SKMessageFormatter,
    bufferSize: Int,
) : SKConnection {

    /**
     * As messages are often very small, we must flush after every send or else
     * the endpoint would become stuck waiting for more data to fill the buffer before sending.
     */
    private val outputStream = s.openWriteChannel(autoFlush = true)
    private val inputStream = s.openReadChannel()
    private val buffer = ByteBuffer.allocate(bufferSize)
    private val queue = mutableListOf<SKMessage>()
    private val logger = KotlinLogging.logger {}

    override fun close() {
        s.close()
    }

    override suspend fun readMsg(): SKMessage {
        return if (queue.isNotEmpty()) {
            queue.removeFirst()
        } else {
            inputStream.read(0) {
                println(it.remaining())
                if (buffer.remaining() < it.remaining()) {
                    logger.error { "Allocated buffer is too small: ${buffer.remaining()}. Needs to be at least ${it.remaining()}, but ${it.capacity()} is recommended." }
                }
                buffer.put(it)
            }
            buffer.flip()

            if (!buffer.hasRemaining()) {
                // Connection lost
                throw ClosedReceiveChannelException("")
            }

            // Fully process the buffer's content
            var o = objFormatter.fromBytes(buffer)
            while (o.isPresent) {
                queue.add(o.get()) // populate queue
                o = objFormatter.fromBytes(buffer)
            }
            buffer.compact()
            readMsg()
        }
    }

    override suspend fun writeMsg(msg: SKMessage) {
        val msgBytes = objFormatter.toBytes(msg)
        outputStream.writeFully(msgBytes)
    }
}
