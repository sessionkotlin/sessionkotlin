package org.david.sessionkotlin_lib.backend.socket

import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import org.david.sessionkotlin_lib.backend.SKBinaryEndpoint
import org.david.sessionkotlin_lib.backend.SKMessage
import org.david.sessionkotlin_lib.backend.SKMessageFormatter
import java.nio.ByteBuffer

internal class SKBinarySocketEndpoint(
    private var s: Socket,
    private val objFormatter: SKMessageFormatter,
) : SKBinaryEndpoint {

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
