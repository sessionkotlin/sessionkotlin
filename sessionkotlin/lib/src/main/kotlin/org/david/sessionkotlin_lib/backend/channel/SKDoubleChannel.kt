package org.david.sessionkotlin_lib.backend.channel

import kotlinx.coroutines.channels.Channel
import org.david.sessionkotlin_lib.backend.SKMessage
import java.io.Closeable

internal data class SKDoubleChannel(val input: Channel<SKMessage>, val output: Channel<SKMessage>) : Closeable {
    suspend fun receive(): SKMessage = input.receive()
    suspend fun send(msg: SKMessage) = output.send(msg)
    override fun close() {
        output.close()
    }
}
