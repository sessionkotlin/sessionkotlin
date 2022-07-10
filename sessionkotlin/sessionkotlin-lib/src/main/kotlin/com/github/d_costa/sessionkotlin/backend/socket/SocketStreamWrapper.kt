package com.github.d_costa.sessionkotlin.backend.socket

import io.ktor.utils.io.*
import mu.KotlinLogging
import java.nio.ByteBuffer

internal class SocketStreamWrapper(
    private var inputStream: ByteReadChannel,
    private var outputStream: ByteWriteChannel,
    bufferSize: Int,
) : SocketIO {
    private val logger = KotlinLogging.logger {}

    override suspend fun readBytes(destBuffer: ByteBuffer) {
        inputStream.read(0) {
            if (destBuffer.remaining() < it.remaining()) {
                logger.error { "Allocated buffer is too small: ${destBuffer.remaining()}. Needs to be at least ${it.remaining()}, but ${it.capacity()} is recommended." }
            }
            destBuffer.put(it)
        }
    }

    override suspend fun writeBytes(srcBuffer: ByteBuffer) {
        outputStream.writeFully(srcBuffer)
    }

    override fun close() {
        outputStream.close()
    }
}
