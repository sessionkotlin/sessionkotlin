package org.david.sessionkotlin_lib.api

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import org.david.sessionkotlin_lib.dsl.SKRole
import java.io.Closeable
import java.nio.ByteBuffer
import java.util.*

public class NotConnectedException(role: SKRole) : RuntimeException("Not connected to $role")
public class AlreadyConnectedException(role: SKRole) : RuntimeException("Already connected to $role")

public class SKChannel {
    internal val chan = Channel<SKMessage>(Channel.UNLIMITED)
}

public class SKEndpoint : AutoCloseable {
    private val connections = mutableMapOf<SKRole, SKBinaryEndpoint>()
    private val selectorManager = ActorSelectorManager(Dispatchers.IO)

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
        connections[role] = SKBinarySocketEndpoint(socket)
    }

    public suspend fun accept(role: SKRole, port: Int) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        val socket = aSocket(selectorManager)
            .tcp()
            .bind(InetSocketAddress("localhost", port))
            .accept()

        connections[role] = SKBinarySocketEndpoint(socket)
    }

    public fun connect(role: SKRole, chan: SKChannel) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        connections[role] = SKBinaryChannelEndpoint(chan.chan)
    }
}

internal interface SKBinaryEndpoint : Closeable {
    suspend fun readMsg(): SKMessage
    suspend fun writeMsg(msg: SKMessage)
}

internal class SKBinarySocketEndpoint(private var s: Socket) : SKBinaryEndpoint {

    // ObjectOutputStream's constructor must always come before ObjectInputStream's constructor
    private val outputStream = s.openWriteChannel(autoFlush = true)
    private val inputStream = s.openReadChannel()
    private val objFormatter = ObjectFormatter()

    override fun close() {
        s.close()
    }

    override suspend fun readMsg(): SKMessage {
        val size = inputStream.readInt()
        println("read: $size")
        val b = ByteBuffer.wrap(ByteArray(size))

        var total = 0
        while(total < size) {
            println("trying to read: " + (size - total))
            inputStream.read(size - total) {
                total += it.remaining()
                println("read remaining: ${it.remaining()}")
                b.put(it)
            }
        }
        return objFormatter.fromBytes(b.array())
    }

    override suspend fun writeMsg(msg: SKMessage) {
        val msgBytes = objFormatter.toBytes(msg)
        println("write: " + msgBytes.size)
        outputStream.writeInt(msgBytes.size)

        var remainder = msgBytes.size
        while(remainder > 0) {
            println("trying to write: " + remainder)
            outputStream.write(remainder) {
                remainder -= it.remaining()
                println("write remaining: ${it.remaining()}")
                it.put(msgBytes)
            }
        }

    }
}

internal class SKBinaryChannelEndpoint(private var chan: Channel<SKMessage>) : SKBinaryEndpoint {
    override suspend fun readMsg(): SKMessage = chan.receive()

    override suspend fun writeMsg(msg: SKMessage) {
        chan.send(msg)
    }

    override fun close() {
        chan.close()
    }
}
