package backend

import com.github.d_costa.sessionkotlin.api.SKGenRole
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.message.ObjectFormatter
import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.backend.message.SKMessageFormatter
import com.github.d_costa.sessionkotlin.backend.message.SKPayload
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import java.net.BindException
import java.nio.ByteBuffer
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SKMPEndpointTest {
    companion object {
        object A : SKGenRole()
        object B : SKGenRole()
        object C : SKGenRole()

        val payloads = listOf<Any>("Hello world", "stuff", 10, 10L, 2.3)

        fun getFormatter() = object : SKMessageFormatter {
            val f = ObjectFormatter()
            override fun toBytes(msg: SKMessage) = f.toBytes(msg)
            override fun fromBytes(b: ByteBuffer) = f.fromBytes(b)
        }
    }

    @Test
    fun `test explicit objectFormatter with sockets`() {
        val c = Channel<Int>()

        runBlocking {
            launch {
                SKMPEndpoint(getFormatter()).use { endpoint ->
                    val s = SKMPEndpoint.bind()
                    c.send(s.port)
                    endpoint.accept(B, s)
                    aProtocol(endpoint)
                }
            }
            launch {
                SKMPEndpoint(getFormatter()).use { endpoint ->
                    endpoint.request(A, "localhost", c.receive())
                    bProtocol(endpoint)
                }
            }
        }
    }

    private suspend fun bProtocol(endpoint: SKMPEndpoint) {
        for (p in payloads) {
            val received = endpoint.receive(A) as SKPayload<*>
            assertEquals(received.payload, p)
        }
        for (p in payloads) {
            endpoint.send(A, SKPayload(p))
        }
    }

    private suspend fun aProtocol(endpoint: SKMPEndpoint) {
        for (p in payloads) {
            endpoint.send(B, SKPayload(p))
            delay(10)
        }
        for (p in payloads) {
            val received = endpoint.receive(B) as SKPayload<*>
            delay(10)
            assertEquals(received.payload, p)
        }
    }

    @Test
    fun `test double bind`() {
        runBlocking {
            launch {
                SKMPEndpoint().use {
                    SKMPEndpoint.bind()
                    SKMPEndpoint.bind()
                }
            }
        }
    }

    @Test
    fun `test accept port after bind`() {
        assertFailsWith<BindException> {
            runBlocking {
                launch {
                    SKMPEndpoint().use { endpoint ->
                        val s = SKMPEndpoint.bind()
                        endpoint.accept(B, s.port)
                    }
                }
            }
        }
    }
}
