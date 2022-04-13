package org.david.sessionkotlin_lib.backend

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import org.david.sessionkotlin_lib.backend.exception.AlreadyConnectedException
import org.david.sessionkotlin_lib.backend.exception.BinaryEndpointsException
import org.david.sessionkotlin_lib.backend.exception.NotConnectedException
import org.david.sessionkotlin_lib.dsl.SKRole
import java.io.Closeable
import java.nio.ByteBuffer

internal data class SKBinaryChannel(val input: Channel<SKMessage>, val output: Channel<SKMessage>) : Closeable {
    suspend fun receive(): SKMessage = input.receive()
    suspend fun send(msg: SKMessage) = output.send(msg)
    override fun close() {
        output.close()
    }
}

/**
 * Creates a bidirectional channel between two roles.
 */
public class SKChannel(private val role1: SKRole, private val role2: SKRole) {

    private val chanA = Channel<SKMessage>(Channel.UNLIMITED)
    private val chanB = Channel<SKMessage>(Channel.UNLIMITED)

    private val mapping = mapOf(
        role1 to SKBinaryChannel(chanA, chanB),
        role2 to SKBinaryChannel(chanB, chanA)
    )

    internal fun getEndpoints(role: SKRole): SKBinaryChannel =
        try {
            mapping.getValue(role)
        } catch (e: NoSuchElementException) {
            throw BinaryEndpointsException(role, role1, role2)
        }
}

public class SKEndpoint : AutoCloseable {
    private val connections = mutableMapOf<SKRole, SKBinaryEndpoint>()
    private val selectorManager = ActorSelectorManager(Dispatchers.Default)
    private val objectFormatter = ObjectFormatter()

    override fun close() {
        for (ch in connections.values) {
            ch.close()
        }
    }

    internal suspend fun send(role: SKRole, msg: SKMessage) {
        val ch = connections[role] ?: throw NotConnectedException(role)
        ch.writeMsg(msg)
    }

    internal suspend fun receive(role: SKRole): SKMessage {
        val ch = connections[role] ?: throw NotConnectedException(role)
        return ch.readMsg()
    }

    public suspend fun request(role: SKRole, hostname: String, port: Int) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        val socket = aSocket(selectorManager)
            .tcp()
            .connect(hostname, port)
        connections[role] = SKBinarySocketEndpoint(socket, objectFormatter)
    }

    public suspend fun accept(role: SKRole, port: Int) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        val socket = aSocket(selectorManager)
            .tcp()
            .bind(InetSocketAddress("localhost", port))
            .accept()

        connections[role] = SKBinarySocketEndpoint(socket, objectFormatter)
    }

    public fun connect(role: SKRole, chan: SKChannel) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        connections[role] = SKBinaryChannelEndpoint(chan.getEndpoints(role))
    }
}

internal interface SKBinaryEndpoint : Closeable {
    suspend fun readMsg(): SKMessage
    suspend fun writeMsg(msg: SKMessage)
}

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

internal class SKBinaryChannelEndpoint(private var chan: SKBinaryChannel) : SKBinaryEndpoint {
    override suspend fun readMsg(): SKMessage = chan.receive()

    override suspend fun writeMsg(msg: SKMessage) {
        chan.send(msg)
    }

    override fun close() {
        chan.close()
    }
}
