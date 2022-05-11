package com.github.d_costa.sessionkotlin.backend

import com.github.d_costa.sessionkotlin.api.SKGenRole
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.channel.SKChannelEndpoint
import com.github.d_costa.sessionkotlin.backend.exception.AlreadyConnectedException
import com.github.d_costa.sessionkotlin.backend.exception.NotConnectedException
import com.github.d_costa.sessionkotlin.backend.exception.ReadClosedChannelException
import com.github.d_costa.sessionkotlin.backend.socket.SKSocketEndpoint
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException

/**
 * A Multiparty Endpoint.
 *
 * Provides operations to connect to other endpoints along channels or sockets,
 * and to send and receive messages.
 *
 */
public open class SKMPEndpoint : AutoCloseable {
    /**
     * Maps generated roles to the individual endpoint that must be used for communication.
     */
    private val connections = mutableMapOf<SKGenRole, SKEndpoint>()

    /**
     * Service that manages NIO selectors and selection threads.
     */
    private val selectorManager = ActorSelectorManager(Dispatchers.IO)
    private val objectFormatter = ObjectFormatter()

    /**
     * Close all individual endpoints.
     */
    override fun close() {
        for (ch in connections.values) {
            ch.close()
        }
    }

    /**
     * Send a message [msg] to [role].
     */
    internal suspend fun send(role: SKGenRole, msg: SKMessage): Unit = sendProtected(role, msg)

    /**
     * Send a message [msg] to [role].
     *
     * This **protected** method is necessary since kotlin's *protected* has a different meaning
     * than java's.
     *
     * We need this workaround because generated classes cannot access the **internal** variant
     * and the other option was to make this method **public**.
     *
     */
    protected suspend fun sendProtected(role: SKGenRole, msg: SKMessage) {
        val ch = connections[role] ?: throw NotConnectedException(role)
        ch.writeMsg(msg)
    }

    /**
     * Receives a message from [role].
     */
    internal suspend fun receive(role: SKGenRole): SKMessage = receiveProtected(role)

    /**
     * Receive a message from [role].
     *
     * This **protected** method is necessary since kotlin's *protected* has a different meaning
     * than java's.
     *
     * We need this workaround because generated classes cannot access the **internal** variant
     * and the other option was to make this method **public**.
     *
     */
    protected suspend fun receiveProtected(role: SKGenRole): SKMessage {
        try {
            val ch = connections[role] ?: throw NotConnectedException(role)
            return ch.readMsg()
        } catch (e: ClosedReceiveChannelException) {
            throw ReadClosedChannelException(role)
        }
    }

    /**
     * Request a TCP connection to [role] on [hostname]:[port].
     */
    public suspend fun request(role: SKGenRole, hostname: String, port: Int) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        val socket = aSocket(selectorManager)
            .tcp()
            .connect(hostname, port)
        connections[role] = SKSocketEndpoint(socket, objectFormatter)
    }

    /**
     * Request a TCP connection to [role] on [port].
     */
    public suspend fun accept(role: SKGenRole, port: Int) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        val socket = aSocket(selectorManager)
            .tcp()
            .bind(InetSocketAddress("localhost", port))
            .accept()

        connections[role] = SKSocketEndpoint(socket, objectFormatter)
    }

    /**
     * Connect to [role] over [chan].
     */
    public fun connect(role: SKGenRole, chan: SKChannel) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        connections[role] = SKChannelEndpoint(chan.getEndpoints(role))
    }
}