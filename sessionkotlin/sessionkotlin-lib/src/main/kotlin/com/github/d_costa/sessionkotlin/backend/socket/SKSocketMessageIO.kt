package com.github.d_costa.sessionkotlin.backend.socket

import com.github.d_costa.sessionkotlin.backend.endpoint.MessageIO
import com.github.d_costa.sessionkotlin.backend.endpoint.SocketWrapper
import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.backend.message.SKMessageFormatter
import io.ktor.network.sockets.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import java.nio.ByteBuffer

/**
 * Endpoint implementation with sockets.
 */
internal class SKSocketMessageIO(
    internal var s: Socket,
    private val objFormatter: SKMessageFormatter,
    private val bufferSize: Int,
) : MessageIO {

    private val buffer = ByteBuffer.allocate(bufferSize)

    /**
     * As messages are often very small, we must flush after every send or else
     * the endpoint would become stuck waiting for more data to fill the buffer before sending.
     */
    private val outputStream = s.openWriteChannel(autoFlush = true)
    private val inputStream = s.openReadChannel()
    private var socketIO: SocketIO = SocketStreamWrapper(inputStream, outputStream)

    /**
     * Incoming messages
     */
    private val queue = mutableListOf<SKMessage>()

    override fun close() {
        socketIO.close()
        s.close()
    }

    override tailrec suspend fun readMsg(): SKMessage {
        return if (queue.isNotEmpty()) {
            queue.removeFirst()
        } else {
            socketIO.readBytes(buffer)

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
        socketIO.writeBytes(ByteBuffer.wrap(objFormatter.toBytes(msg)))
    }

    suspend fun wrapSocket(wrapper: SocketWrapper) {
        wrapper.init(socketIO, bufferSize)
        wrapper.handshake()
        socketIO = wrapper
    }
}
