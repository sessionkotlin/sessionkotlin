package com.github.d_costa.sessionkotlin.backend.socket

import java.io.Closeable
import java.nio.ByteBuffer

public interface SocketIO : Closeable {
    public suspend fun readBytes(destBuffer: ByteBuffer)
    public suspend fun writeBytes(srcBuffer: ByteBuffer)
}
