package backend

import com.github.d_costa.sessionkotlin.api.SKGenRole
import com.github.d_costa.sessionkotlin.backend.channel.BinaryConnectionException
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.AlreadyConnectedException
import com.github.d_costa.sessionkotlin.backend.endpoint.NotConnectedException
import com.github.d_costa.sessionkotlin.backend.endpoint.ReadClosedConnectionException
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.message.SKPayload
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SocketsTest {
    companion object {
        object A : SKGenRole()
        object B : SKGenRole()
        object C : SKGenRole()

        val payloads = listOf<Any>("Hello world", 10, 10L, 2.3)
    }

    @Test
    fun `test sockets`() {
        val c = Channel<Int>()
        runBlocking {
            launch {
                SKMPEndpoint().use { endpoint ->
                    val s = SKMPEndpoint.bind()
                    c.send(s.port)
                    endpoint.accept(B, s)

                    for (p in payloads) {
                        endpoint.send(B, SKPayload(p))
                    }
                    for (p in payloads) {
                        val received = endpoint.receive(B) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                }
            }
            launch {
                SKMPEndpoint().use { endpoint ->
                    endpoint.request(A, "localhost", c.receive())

                    for (p in payloads) {
                        val received = endpoint.receive(A) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                    for (p in payloads) {
                        endpoint.send(A, SKPayload(p))
                    }
                }
            }
        }
    }

    @Test
    fun `test channels`() {
        val chan = SKChannel(A, B)
        runBlocking {
            // A
            launch {
                SKMPEndpoint().use { endpoint ->
                    endpoint.connect(B, chan)
                    for (p in payloads) {
                        endpoint.send(B, SKPayload(p))
                    }
                    for (p in payloads) {
                        val received = endpoint.receive(B) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                }
            }

            // B
            launch {
                SKMPEndpoint().use { endpoint ->
                    endpoint.connect(A, chan)
                    for (p in payloads) {
                        val received = endpoint.receive(A) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                    for (p in payloads) {
                        endpoint.send(A, SKPayload(p))
                    }
                }
            }
        }
    }

    @Test
    fun `test channels and sockets`() {
        val chan = SKChannel(B, C)
        val c = Channel<Int>()
        runBlocking {
            // A
            launch {
                SKMPEndpoint().use { endpoint ->
                    val s = SKMPEndpoint.bind()
                    c.send(s.port)
                    endpoint.accept(B, s)
                    for (p in payloads) {
                        val received = endpoint.receive(B) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                    for (p in payloads) {
                        endpoint.send(B, SKPayload(p))
                    }
                }
            }
            // B
            launch {
                SKMPEndpoint().use { endpoint ->
                    endpoint.request(A, "localhost", c.receive())
                    endpoint.connect(C, chan)

                    for (p in payloads) {
                        endpoint.send(A, SKPayload(p))
                    }
                    for (p in payloads) {
                        val received = endpoint.receive(A) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                    for (p in payloads) {
                        endpoint.send(C, SKPayload(p))
                    }
                    for (p in payloads) {
                        val received = endpoint.receive(C) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                }
            }

            // C
            launch {
                SKMPEndpoint().use { endpoint ->
                    endpoint.connect(B, chan)

                    for (p in payloads) {
                        val received = endpoint.receive(B) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                    for (p in payloads) {
                        endpoint.send(B, SKPayload(p))
                    }
                }
            }
        }
    }

    @Test
    fun `already connected connect`() {
        val chan = SKChannel(A, B)
        assertFailsWith<AlreadyConnectedException> {
            runBlocking {
                launch {
                    SKMPEndpoint().use { endpoint ->
                        endpoint.connect(B, chan)
                        endpoint.connect(B, chan)
                    }
                }
            }
        }
    }

    @Test
    fun `already connected accept`() {
        val c = Channel<Int>()
        assertFailsWith<AlreadyConnectedException> {
            runBlocking {
                launch {
                    SKMPEndpoint().use { endpoint ->
                        val s = SKMPEndpoint.bind()
                        c.send(s.port)
                        endpoint.accept(B, s)
                        endpoint.accept(B, s)
                    }
                }
                launch {
                    SKMPEndpoint().use { endpoint ->
                        endpoint.request(B, "localhost", c.receive())
                    }
                }
            }
        }
    }

    @Test
    fun `already connected request`() {
        assertFailsWith<AlreadyConnectedException> {
            val c = Channel<Int>(2)
            runBlocking {
                launch {
                    SKMPEndpoint().use { endpoint ->
                        val s = SKMPEndpoint.bind()
                        c.send(s.port)
                        c.send(s.port)
                        endpoint.accept(B, s)
                    }
                }
                launch {
                    SKMPEndpoint().use { endpoint ->
                        endpoint.request(B, "localhost", c.receive())
                        endpoint.request(B, "localhost", c.receive())
                    }
                }
            }
        }
    }

    @Test
    fun `not connected send`() {
        assertFailsWith<NotConnectedException> {
            runBlocking {
                launch {
                    SKMPEndpoint().use { endpoint ->
                        endpoint.send(B, SKPayload(""))
                    }
                }
            }
        }
    }

    @Test
    fun `not connected receive`() {
        assertFailsWith<NotConnectedException> {
            runBlocking {
                launch {
                    SKMPEndpoint().use { endpoint ->
                        endpoint.receive(B)
                    }
                }
            }
        }
    }

    @Test
    fun `binary endpoint not found`() {
        val chan = SKChannel(A, B)
        assertFailsWith<BinaryConnectionException> {
            runBlocking {
                launch {
                    SKMPEndpoint().use { endpoint ->
                        endpoint.connect(C, chan)
                    }
                }
            }
        }
    }

    @Test
    fun `test read from closed channel`() {
        val c = Channel<Int>()

        assertFailsWith<ReadClosedConnectionException> {
            runBlocking {
                launch {
                    SKMPEndpoint().use {
                        val s = SKMPEndpoint.bind()
                        c.send(s.port)
                        it.accept(A, s)
                        it.close()
                    }
                }
                launch {
                    SKMPEndpoint().use {
                        val port = c.receive()
                        it.request(B, "localhost", port)
                        it.receive(B)
                    }
                }
            }
        }
    }

    @Test
    fun `test send branch`() {
        val chan = SKChannel()

        runBlocking {
            launch {
                SKMPEndpoint().use { endpoint ->
                    endpoint.connect(B, chan)
                    endpoint.send(B, SKPayload(0, "b1"))
                }
            }
            launch {
                SKMPEndpoint().use { endpoint ->
                    endpoint.connect(A, chan)
                    assertEquals("b1", (endpoint.receive(A) as SKPayload<*>).branch)
                }
            }
        }
    }

    @Test
    fun `test reuse port`() {
        val c = Channel<Int>(2)
        runBlocking {
            launch {
                SKMPEndpoint().use { endpoint ->
                    val s = SKMPEndpoint.bind()
                    c.send(s.port)
                    c.send(s.port)
                    endpoint.accept(A, s)
                    endpoint.accept(B, s)
                }
            }
            launch {
                SKMPEndpoint().use { endpoint ->
                    endpoint.request(C, "localhost", c.receive())
                }
            }
            launch {
                SKMPEndpoint().use { endpoint ->
                    endpoint.request(C, "localhost", c.receive())
                }
            }
        }
    }
}
