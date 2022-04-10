package org.david.sessionkotlin_lib.api

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import org.david.sessionkotlin_lib.dsl.SKRole
import java.io.Closeable
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket

public class NotConnectedException(role: SKRole) : RuntimeException("Not connected to $role")
public class AlreadyConnectedException(role: SKRole) : RuntimeException("Already connected to $role")

public class SKEndpoint : AutoCloseable {
    private val connections = mutableMapOf<SKRole, SKBinaryEndpoint>()

    override fun close() {
        for (ch in connections.values) {
            ch.close()
        }
    }

    internal fun send(role: SKRole, msg: SKMessage) {
        val ch = connections[role] ?: throw NotConnectedException(role)
        ch.writeMsg(msg)
    }

    internal fun receive(role: SKRole): SKMessage {
        val ch = connections[role] ?: throw NotConnectedException(role)
        return ch.readMsg()
    }

    public fun request(role: SKRole, hostname: String, port: Int) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        connections[role] = SKClientSocket(hostname, port)
    }

    public fun accept(role: SKRole, port: Int): Int {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        val ss = SKServerSocket(port)
        connections[role] = ss
        return ss.getLocalPort()
    }

    public fun connect(role: SKRole, chan: Channel<SKMessage>) {
        if (role in connections) {
            throw AlreadyConnectedException(role)
        }
        connections[role] = SKBinaryChannelEndpoint(chan)
    }
}

internal interface SKBinaryEndpoint : Closeable {
    fun readMsg(): SKMessage
    fun writeMsg(msg: SKMessage)
}

internal abstract class SKBinarySocketEndpoint(private var s: Socket) : SKBinaryEndpoint {

    // ObjectOutputStream's constructor must always come before ObjectInputStream's constructor
    private val outputStream = ObjectOutputStream(s.getOutputStream())
    private val inputStream = ObjectInputStream(s.getInputStream())

    override fun close() {
        s.close()
    }

    override fun readMsg(): SKMessage {
        return inputStream.readObject() as SKMessage
    }

    override fun writeMsg(msg: SKMessage) {
        outputStream.writeObject(msg)
    }

    fun getLocalPort() = s.localPort
}

internal class SKServerSocket(port: Int) : SKBinarySocketEndpoint(ServerSocket(port).accept())
internal class SKClientSocket(hostname: String, port: Int) : SKBinarySocketEndpoint(Socket(hostname, port))

internal class SKBinaryChannelEndpoint(private var chan: Channel<SKMessage>) : SKBinaryEndpoint {
    private suspend fun suspendedReadMsg(): SKMessage = chan.receive()

    private suspend fun suspendedWriteMsg(msg: SKMessage) {
        chan.send(msg)
    }

    override fun readMsg(): SKMessage {
        return runBlocking {
            return@runBlocking suspendedReadMsg()
        }
    }

    override fun writeMsg(msg: SKMessage) {
        runBlocking {
            suspendedWriteMsg(msg)
        }
    }

    override fun close() {
        chan.close()
    }
}
