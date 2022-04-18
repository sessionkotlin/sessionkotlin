package org.david.sessionkotlin_lib.backend

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import org.david.sessionkotlin_lib.api.SKGenRole
import org.david.sessionkotlin_lib.backend.channel.SKBinaryChannelEndpoint
import org.david.sessionkotlin_lib.backend.channel.SKChannel
import org.david.sessionkotlin_lib.backend.exception.AlreadyConnectedException
import org.david.sessionkotlin_lib.backend.exception.NotConnectedException
import org.david.sessionkotlin_lib.backend.exception.ReadClosedChannelException
import org.david.sessionkotlin_lib.backend.socket.SKBinarySocketEndpoint

public class SKMPEndpoint : AutoCloseable {
    private val connections = mutableMapOf<SKGenRole, SKBinaryEndpoint>()
    private val selectorManager = ActorSelectorManager(Dispatchers.IO)
    private val objectFormatter = ObjectFormatter()

    override fun close() {
        for (ch in connections.values) {
            ch.close()
        }
    }

    internal suspend fun send(role: SKGenRole, msg: SKMessage) {
        val ch = connections[role] ?: throw NotConnectedException(role)
        ch.writeMsg(msg)
    }

    internal suspend fun receive(role: SKGenRole): SKMessage {
        try {
            val ch = connections[role] ?: throw NotConnectedException(role)
            return ch.readMsg()
        } catch (e: ClosedReceiveChannelException) {
            throw ReadClosedChannelException(role)
        }
    }

    public suspend fun request(role: SKGenRole, hostname: String, port: Int) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        val socket = aSocket(selectorManager)
            .tcp()
            .connect(hostname, port)
        connections[role] = SKBinarySocketEndpoint(socket, objectFormatter)
    }

    public suspend fun accept(role: SKGenRole, port: Int) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        val socket = aSocket(selectorManager)
            .tcp()
            .bind(InetSocketAddress("localhost", port))
            .accept()

        connections[role] = SKBinarySocketEndpoint(socket, objectFormatter)
    }

    public fun connect(role: SKGenRole, chan: SKChannel) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        connections[role] = SKBinaryChannelEndpoint(chan.getEndpoints(role))
    }
}
