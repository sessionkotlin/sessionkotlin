package org.david.sessionkotlin.backend.channel

import org.david.sessionkotlin.backend.SKBinaryEndpoint
import org.david.sessionkotlin.backend.SKMessage

internal class SKBinaryChannelEndpoint(private var chan: SKDoubleChannel) : SKBinaryEndpoint {
    override suspend fun readMsg(): SKMessage = chan.receive()

    override suspend fun writeMsg(msg: SKMessage) {
        chan.send(msg)
    }

    override fun close() {
        chan.close()
    }
}
