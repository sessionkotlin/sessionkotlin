package com.github.d_costa.sessionkotlin.backend.endpoint

import com.github.d_costa.sessionkotlin.backend.socket.SocketIO
import java.nio.ByteBuffer
import kotlin.properties.Delegates

public abstract class SocketWrapper : SocketIO {
    protected lateinit var socketIO: SocketIO
    private var bufferSize: Int by Delegates.notNull<Int>()

    internal fun init(socketIO: SocketIO, bufferSize: Int) {
        this.socketIO = socketIO
        this.bufferSize = bufferSize
    }

    /**
     * Wrap [msgBytes] before passing it to the transport protocol.
     *
     * [buffer] is ready to be read. Must return a ByteBuffer ready to be read as well.
     *
     */
    public abstract suspend fun wrapBytes(buffer: ByteBuffer): ByteBuffer

    /**
     * Unwrap [wrappedBytes] before passing it to the application layer.
     */
    public abstract suspend fun unwrapBytes(wrappedBytes: ByteBuffer): ByteBuffer

    public abstract suspend fun handshake()

    override suspend fun readBytes(destBuffer: ByteBuffer) {
        val b = ByteBuffer.allocate(bufferSize)
        socketIO.readBytes(b)
        b.flip()

        val wb = unwrapBytes(b)
        destBuffer.put(wb)

        wb.compact()
    }

    override suspend fun writeBytes(srcBuffer: ByteBuffer) {
        val wrappedBytes = wrapBytes(srcBuffer)
        socketIO.writeBytes(wrappedBytes)

        wrappedBytes.compact()
    }

    override fun close() {
        socketIO.close()
    }
}

public enum class ConnectionEnd {
    Client, Server
}
