package com.github.sessionkotlin.lib.backend.channel

import com.github.sessionkotlin.lib.backend.message.SKMessage
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import java.io.Closeable

/**
 * Uses two channels for two-way communication.
 * [input] is used to receive messages and [output] to send.
 *
 * Two channels must be used to prevent the sender from reading its own messages.
 */
internal data class SKDoubleChannel(val input: ReceiveChannel<SKMessage>, val output: SendChannel<SKMessage>) : Closeable {
    suspend fun receive(): SKMessage = input.receive()
    suspend fun send(msg: SKMessage) = output.send(msg)

    /**
     * Closes the output channel.
     */
    override fun close() {
        output.close()
    }
}
