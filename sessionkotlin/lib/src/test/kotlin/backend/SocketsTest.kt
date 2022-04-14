package backend

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.david.sessionkotlin_lib.api.SKGenRole
import org.david.sessionkotlin_lib.backend.SKMPEndpoint
import org.david.sessionkotlin_lib.backend.SKPayload
import org.david.sessionkotlin_lib.backend.channel.SKChannel
import org.david.sessionkotlin_lib.backend.exception.AlreadyConnectedException
import org.david.sessionkotlin_lib.backend.exception.BinaryEndpointsException
import org.david.sessionkotlin_lib.backend.exception.NotConnectedException
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SocketsTest {
    companion object {
        object A : SKGenRole
        object B : SKGenRole
        object C : SKGenRole

        val payloads = listOf<Any>("Hello world", 10, 10L, 2.3)
        private var atomicPort = AtomicInteger(9000)
        fun nextPort() = atomicPort.incrementAndGet()
    }

    @Test
    fun `test sockets`() {
        val port = nextPort()

        runBlocking {
            launch {
                SKMPEndpoint().use { endpoint ->
                    endpoint.accept(B, port)

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
                    endpoint.request(A, "localhost", port)

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
        val port = nextPort()

        runBlocking {
            // A
            launch {
                SKMPEndpoint().use { endpoint ->
                    endpoint.accept(B, port)
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
                    endpoint.request(A, "localhost", port)
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
        val port = nextPort()
        assertFailsWith<AlreadyConnectedException> {
            runBlocking {
                launch {
                    SKMPEndpoint().use { endpoint ->
                        endpoint.accept(B, port)
                        endpoint.accept(B, port)
                    }
                }
                launch {
                    SKMPEndpoint().use { endpoint ->
                        endpoint.request(B, "localhost", port)
                    }
                }
            }
        }
    }

    @Test
    fun `already connected request`() {
        val port = nextPort()
        assertFailsWith<AlreadyConnectedException> {
            runBlocking {
                launch {
                    SKMPEndpoint().use { endpoint ->
                        endpoint.accept(B, port)
                    }
                }
                launch {
                    SKMPEndpoint().use { endpoint ->
                        endpoint.request(B, "localhost", port)
                        endpoint.request(B, "localhost", port)
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
        assertFailsWith<BinaryEndpointsException> {
            runBlocking {
                launch {
                    SKMPEndpoint().use { endpoint ->
                        endpoint.connect(C, chan)
                    }
                }
            }
        }
    }
}
