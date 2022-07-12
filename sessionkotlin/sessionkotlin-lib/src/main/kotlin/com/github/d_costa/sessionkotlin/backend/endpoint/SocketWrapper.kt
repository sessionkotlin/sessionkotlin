package com.github.d_costa.sessionkotlin.backend.endpoint

import com.github.d_costa.sessionkotlin.backend.socket.SocketIO
import java.nio.ByteBuffer
import kotlin.properties.Delegates

/**
 * Socket IO capable of performing an initial handshake and
 * wrapping/unwrapping data before delivering to the next/previous layer.
 */
public abstract class SocketWrapper : SocketIO {
    protected lateinit var socketIO: SocketIO
    private var bufferSize: Int by Delegates.notNull()

    internal fun init(socketIO: SocketIO, bufferSize: Int) {
        this.socketIO = socketIO
        this.bufferSize = bufferSize
    }

    /**
     * Wrap [buffer] before passing it to the transport protocol.
     *
     * [buffer] must be ready for reading. Must return a ByteBuffer ready to be read as well.
     *
     */
    public abstract suspend fun wrapBytes(buffer: ByteBuffer): ByteBuffer

    /**
     * Unwrap [wrappedBytes] before passing it to the application layer.
     *
     * [wrappedBytes] must be ready for reading. Must return a ByteBuffer ready to be read as well.
     */
    public abstract suspend fun unwrapBytes(wrappedBytes: ByteBuffer): ByteBuffer

    /**
     * Perform any necessary handshakes before wrapping or unwrapping.
     *
     */
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
        socketIO.writeBytes(wrapBytes(srcBuffer))
    }

    override fun close() {
        socketIO.close()
    }
}

public enum class ConnectionEnd {
    Client, Server
}
