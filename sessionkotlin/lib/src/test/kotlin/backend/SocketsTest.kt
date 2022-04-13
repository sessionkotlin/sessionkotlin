package backend

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.david.sessionkotlin_lib.backend.SKChannel
import org.david.sessionkotlin_lib.backend.SKEndpoint
import org.david.sessionkotlin_lib.backend.SKPayload
import org.david.sessionkotlin_lib.backend.exception.AlreadyConnectedException
import org.david.sessionkotlin_lib.backend.exception.BinaryEndpointsException
import org.david.sessionkotlin_lib.backend.exception.NotConnectedException
import org.david.sessionkotlin_lib.dsl.SKRole
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SocketsTest {
    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
        val payloads = listOf<Any>("Hello world", 10, 10L, 2.3)
        private var atomicPort = AtomicInteger(9000)
        fun nextPort() = atomicPort.incrementAndGet()
    }

    @Test
    fun `test sockets`() {
        val port = nextPort()

        runBlocking {
            launch {
                SKEndpoint().use { endpoint ->
                    endpoint.accept(b, port)

                    for (p in payloads) {
                        endpoint.send(b, SKPayload(p))
                    }
                    for (p in payloads) {
                        val received = endpoint.receive(b) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                }
            }
            launch {
                SKEndpoint().use { endpoint ->
                    endpoint.request(a, "localhost", port)

                    for (p in payloads) {
                        val received = endpoint.receive(a) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                    for (p in payloads) {
                        endpoint.send(a, SKPayload(p))
                    }
                }
            }
        }
    }

    @Test
    fun `test channels`() {
        val chan = SKChannel(a, b)
        runBlocking {
            // A
            launch {
                SKEndpoint().use { endpoint ->
                    endpoint.connect(b, chan)
                    for (p in payloads) {
                        endpoint.send(b, SKPayload(p))
                    }
                    for (p in payloads) {
                        val received = endpoint.receive(b) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                }
            }

            // B
            launch {
                SKEndpoint().use { endpoint ->
                    endpoint.connect(a, chan)
                    for (p in payloads) {
                        val received = endpoint.receive(a) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                    for (p in payloads) {
                        endpoint.send(a, SKPayload(p))
                    }
                }
            }
        }
    }

    @Test
    fun `test channels and sockets`() {
        val chan = SKChannel(b, c)
        val port = nextPort()

        runBlocking {
            // A
            launch {
                SKEndpoint().use { endpoint ->
                    endpoint.accept(b, port)
                    for (p in payloads) {
                        val received = endpoint.receive(b) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                    for (p in payloads) {
                        endpoint.send(b, SKPayload(p))
                    }
                }
            }

            // B
            launch {
                SKEndpoint().use { endpoint ->
                    endpoint.request(a, "localhost", port)
                    endpoint.connect(c, chan)

                    for (p in payloads) {
                        endpoint.send(a, SKPayload(p))
                    }
                    for (p in payloads) {
                        val received = endpoint.receive(a) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                    for (p in payloads) {
                        endpoint.send(c, SKPayload(p))
                    }
                    for (p in payloads) {
                        val received = endpoint.receive(c) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                }
            }

            // C
            launch {
                SKEndpoint().use { endpoint ->
                    endpoint.connect(b, chan)

                    for (p in payloads) {
                        val received = endpoint.receive(b) as SKPayload<*>
                        assertEquals(received.payload, p)
                    }
                    for (p in payloads) {
                        endpoint.send(b, SKPayload(p))
                    }
                }
            }
        }
    }

    @Test
    fun `already connected connect`() {
        val chan = SKChannel(a, b)
        assertFailsWith<AlreadyConnectedException> {
            runBlocking {
                launch {
                    SKEndpoint().use { endpoint ->
                        endpoint.connect(b, chan)
                        endpoint.connect(b, chan)
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
                    SKEndpoint().use { endpoint ->
                        endpoint.accept(b, port)
                        endpoint.accept(b, port)
                    }
                }
                launch {
                    SKEndpoint().use { endpoint ->
                        endpoint.request(b, "localhost", port)
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
                    SKEndpoint().use { endpoint ->
                        endpoint.accept(b, port)
                    }
                }
                launch {
                    SKEndpoint().use { endpoint ->
                        endpoint.request(b, "localhost", port)
                        endpoint.request(b, "localhost", port)
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
                    SKEndpoint().use { endpoint ->
                        endpoint.send(b, SKPayload(""))
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
                    SKEndpoint().use { endpoint ->
                        endpoint.receive(b)
                    }
                }
            }
        }
    }

    @Test
    fun `binary endpoint not found`() {
        val chan = SKChannel(a, b)
        assertFailsWith<BinaryEndpointsException> {
            runBlocking {
                launch {
                    SKEndpoint().use { endpoint ->
                        endpoint.connect(c, chan)
                    }
                }
            }
        }
    }
}
