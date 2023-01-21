package com.github.sessionkotlin.lib.backend.channel

import com.github.sessionkotlin.lib.backend.endpoint.MessageIO
import com.github.sessionkotlin.lib.backend.message.SKMessage

/**
 * Endpoint implementation with channels.
 */
internal class SKChannelMessageIO(private var chan: SKDoubleChannel) : MessageIO {

    override suspend fun readMsg(): SKMessage = chan.receive()
    override suspend fun writeMsg(msg: SKMessage) = chan.send(msg)
    override fun close() = chan.close()
}
