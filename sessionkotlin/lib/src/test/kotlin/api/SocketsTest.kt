package api

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import org.david.sessionkotlin_lib.api.SKChannel
import org.david.sessionkotlin_lib.api.SKEndpoint
import org.david.sessionkotlin_lib.api.SKMessage
import org.david.sessionkotlin_lib.api.SKPayload
import org.david.sessionkotlin_lib.dsl.SKRole
import org.junit.jupiter.api.Test
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.test.assertEquals

class SocketsTest {
    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
//        val payloads = listOf<Any>("Hello world", 10, 10L, 2.3)
        val payloads = listOf<Any>("Hello world")
    }

    @Test
    fun `test sockets`() {
        val port = 9000

        runBlocking{
            launch {
                SKEndpoint().use { endpoint ->
                    endpoint.accept(a,  port)
                    for (p in payloads) {
                        endpoint.send(a, SKPayload(p))
                    }
//                    for (p in payloads) {
//                        val received = endpoint.receive(a) as SKPayload<*>
//                        assertEquals(received.payload, p)
//                        println(received)
//                    }
                }
            }
            launch  {
                SKEndpoint().use { endpoint ->
                    endpoint.request(b, "localhost", port)
//
                    for (p in payloads) {
                        val received = endpoint.receive(b) as SKPayload<*>
                        assertEquals(received.payload, p)
                        println(received)
                    }
//                    for (p in payloads) {
//                        endpoint.send(b, SKPayload(p))
//                    }
                }
            }
        }
    }

//    @Test
//    fun `test channels`() {
//        val chan = SKChannel()
//
//        // A
//        val t1 = thread {
//            SKEndpoint().use { endpoint ->
//                endpoint.connect(b, chan)
//                for (p in payloads) {
//                    val received = endpoint.receive(b) as SKPayload<*>
//                    assertEquals(received.payload, p)
////                    println(received)
//                }
//                for (p in payloads) {
//                    endpoint.send(b, SKPayload(p))
//                }
//            }
//        }
//
//        // B
//        val t2 = thread {
//            SKEndpoint().use { endpoint ->
//                endpoint.connect(a, chan)
//                for (p in payloads) {
//                    endpoint.send(a, SKPayload(p))
//                }
//                for (p in payloads) {
//                    val received = endpoint.receive(a) as SKPayload<*>
//                    assertEquals(received.payload, p)
////                    println(received)
//                }
//            }
//        }
//        t1.join()
//        t2.join()
//    }

//    @Test
//    fun `test channels and sockets`() {
//        val chan = Channel<SKMessage>()
//        val port = 9001
//
//        // A
//        val t1 = thread {
//            SKEndpoint().use { endpoint ->
//                endpoint.accept(b, port)
//                for (p in payloads) {
//                    val received = endpoint.receive(b) as SKPayload<*>
//                    assertEquals(received.payload, p)
////                    println(received)
//                }
//                for (p in payloads) {
//                    endpoint.send(b, SKPayload(p))
//                }
//            }
//            throw RuntimeException()
//        }
//
//        // B
//        val t2 = thread {
//            SKEndpoint().use { endpoint ->
//                endpoint.request(a, "localhost", port)
//                endpoint.connect(c, chan)
//
//                for (p in payloads) {
//                    endpoint.send(a, SKPayload(p))
//                }
//                for (p in payloads) {
//                    val received = endpoint.receive(a) as SKPayload<*>
//                    assertEquals(received.payload, p)
////                    println(received)
//                }
//                for (p in payloads) {
//                    endpoint.send(c, SKPayload(p))
//                }
//                for (p in payloads) {
//                    val received = endpoint.receive(c) as SKPayload<*>
//                    assertEquals(received.payload, p)
////                    println(received)
//                }
//            }
//        }
//
//        // C
//        val t3 = thread {
//            SKEndpoint().use { endpoint ->
//                endpoint.connect(b, chan)
//
//                for (p in payloads) {
//                    val received = endpoint.receive(b) as SKPayload<*>
//                    assertEquals(received.payload, p)
//                    println(received)
//                }
//                for (p in payloads) {
//                    endpoint.send(b, SKPayload(p))
//                }
//            }
//        }
//        t1.join()
//        t2.join()
//        t3.join()
//    }
}
