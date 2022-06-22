package demo


import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import simple_server.Client
import simple_server.Server
import simple_server.fluent.SimpleServerClient1
import simple_server.fluent.SimpleServerServer1
import kotlin.random.Random


fun main() {
    fluent()
}

fun fluent() {
    runBlocking {

        // Server
        launch {
            var i = 0
            val ss = SKMPEndpoint.bind(8888)
            do {
                SKMPEndpoint().use { e ->
                    val intBuf = SKBuffer<Int>()
                    e.accept(Client, ss)
                    SimpleServerServer1(e)
                        .receiveFromClient(intBuf)
                        .sendToClient(intBuf.value * 2)
                }
                i++
            } while (true)
        }

        // Client
        repeat(20) { i ->
            launch {
                delay(Random.nextLong(0L, 100L))
                val intBuf = SKBuffer<Int>()
                SKMPEndpoint().use { e ->
                    e.request(Server, "localhost", 8888)
                    SimpleServerClient1(e)
                        .sendToServer(i)
                        .receiveFromServer(intBuf)
                        .also { println("Client $i received ${intBuf.value}") }
                }
            }
        }
    }
}
