package com.github.sessionkotlin.lib.backend.endpoint

import com.github.sessionkotlin.lib.api.SKGenRole
import com.github.sessionkotlin.lib.backend.channel.SKChannel
import com.github.sessionkotlin.lib.backend.channel.SKChannelMessageIO
import com.github.sessionkotlin.lib.backend.message.ObjectFormatter
import com.github.sessionkotlin.lib.backend.message.SKMessage
import com.github.sessionkotlin.lib.backend.message.SKMessageFormatter
import com.github.sessionkotlin.lib.backend.socket.SKSocketMessageIO
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import mu.KotlinLogging

/**
 * A Multiparty Endpoint.
 *
 * Provides operations to connect to other endpoints along channels or sockets,
 * and to send and receive messages.
 *
 * @param msgFormatter (optional) the message formatter. Used to serialize and deserialize messages.
 * @param bufferSize (optional) size of the buffer, when using sockets.
 * @param debug (optional) if true, log sent and received message payloads. Default is false.
 */
public open class SKMPEndpoint(
    private val msgFormatter: SKMessageFormatter = ObjectFormatter(),
    private val bufferSize: Int = defaultBufferSize,
    private val debug: Boolean = false
) : AutoCloseable {
    /**
     * Maps generated roles to the individual endpoint that must be used for communication.
     */
    private val connections = mutableMapOf<SKGenRole, MessageIO>()

    /**
     * Map of ports that are bound and the corresponding server socket
     */
    private val serverSockets = mutableMapOf<Int, ServerSocket>()

    private val logger = KotlinLogging.logger(this::class.simpleName!!)

    public companion object {
        internal const val defaultBufferSize = 16_384

        /**
         * Service that manages NIO selectors and selection threads.
         */
        private val selectorManager = ActorSelectorManager(Dispatchers.IO)

        /**
         * Create a server socket and bind it to [port].
         *
         * Note that SKMPEndpoint will *not* automatically close this socket.
         *
         * @param port the port to bind. Default is zero (0), to use an available port that is automatically allocated.
         */
        public fun bind(port: Int = 0): SKServerSocket {
            return SKServerSocket(
                aSocket(selectorManager)
                    .tcp()
                    .bind(InetSocketAddress("localhost", port))
            )
        }
    }

    /**
     * Close all individual endpoints.
     */
    override fun close() {
        for (ch in connections.values) {
            ch.close()
        }
        for (s in serverSockets.values) {
            s.close()
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
        if (debug) {
            logger.info { "Sent    : ${msg.payload}" }
        }
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
            val msg = ch.readMsg()
            if (debug) {
                logger.info { "Received: ${msg.payload}" }
            }
            return msg
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
        connections[role] = SKSocketMessageIO(socket, msgFormatter, bufferSize)
    }

    /**
     * Bind [port] and accept a connection, attributing it to [role].
     *
     * @return the port that was bound
     */
    public suspend fun accept(role: SKGenRole, port: Int): Int {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        // Create a server socket if none is present for this port
        val serverSocket = serverSockets[port] ?: bind(
            port
        )
            .ss
            .also { serverSockets[port] = it }

        val socket = serverSocket.accept()
        connections[role] = SKSocketMessageIO(socket, msgFormatter, bufferSize)

        return serverSocket.localAddress.toJavaAddress().port
    }

    /**
     * Accept a connection on the provided [serverSocket], attributing it to [role].
     *
     * An instance of [SKServerSocket] is obtained by calling [SKMPEndpoint.bind].
     */
    public suspend fun accept(role: SKGenRole, serverSocket: SKServerSocket) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        val socket = serverSocket.ss.accept()
        connections[role] = SKSocketMessageIO(socket, msgFormatter, bufferSize)
    }

    /**
     * Use [chan] to connect to [role].
     */
    public fun connect(role: SKGenRole, chan: SKChannel) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        connections[role] = SKChannelMessageIO(chan.getEndpoints(role))
    }

    public suspend fun wrap(role: SKGenRole, wrapper: SocketWrapper) {
        val ch = connections[role] ?: throw NotConnectedException(role)

        if (ch !is SKSocketMessageIO) {
            throw WrapperException()
        }

        ch.wrapSocket(wrapper)
    }
}
