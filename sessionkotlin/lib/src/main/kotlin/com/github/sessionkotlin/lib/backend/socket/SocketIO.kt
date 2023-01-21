package com.github.sessionkotlin.lib.backend.socket

import java.io.Closeable
import java.nio.ByteBuffer

/**
 * Generic read/write operations on buffers
 */
public interface SocketIO : Closeable {
    public suspend fun readBytes(destBuffer: ByteBuffer)
    public suspend fun writeBytes(srcBuffer: ByteBuffer)
}
