package com.github.d_costa.sessionkotlin.backend.endpoint

import com.github.d_costa.sessionkotlin.api.SKGenRole
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.channel.SKChannelConnection
import com.github.d_costa.sessionkotlin.backend.message.ObjectFormatter
import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.backend.socket.SKSocketConnection
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
    private val connections = mutableMapOf<SKGenRole, SKConnection>()

    /**
     * Service that manages NIO selectors and selection threads.
     */
    private val selectorManager = ActorSelectorManager(Dispatchers.IO)
    private val objectFormatter = ObjectFormatter()

    /**
     * Map of ports that are bound and the corresponding server socket
     */
    private val serverSockets = mutableMapOf<Int, ServerSocket>()

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
    @Suppress("MemberVisibilityCanBePrivate")
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
    @Suppress("MemberVisibilityCanBePrivate")
    protected suspend fun receiveProtected(role: SKGenRole): SKMessage {
        try {
            val ch = connections[role] ?: throw NotConnectedException(role)
            return ch.readMsg()
        } catch (e: ClosedReceiveChannelException) {
            throw ReadClosedConnectionException(role)
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
        connections[role] = SKSocketConnection(socket, objectFormatter)
    }

    /**
     * Accept a TCP connection from [role] on [port].
     */
    public suspend fun accept(role: SKGenRole, port: Int) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        // Create a server socket if none is present for this port
        val socket = (serverSockets[port] ?: bind(port)).accept()
        connections[role] = SKSocketConnection(socket, objectFormatter)
    }

    /**
     * Creates a socket and binds it.
     *
     */
    private fun bind(port: Int): ServerSocket {
        val serverSocket = aSocket(selectorManager)
            .tcp()
            .bind(InetSocketAddress("localhost", port))
        serverSockets[port] = serverSocket
        return serverSocket
    }

    /**
     * Connect to [role] over [chan].
     */
    public fun connect(role: SKGenRole, chan: SKChannel) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        connections[role] = SKChannelConnection(chan.getEndpoints(role))
    }
}
