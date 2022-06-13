package demo


import Client
import Server
import SimpleServerCallbackEndpointClient
import SimpleServerCallbackEndpointServer
import SimpleServerCallbacksClient
import SimpleServerCallbacksServer
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.random.Random


fun main() {
    runBlocking {

        // Server
        launch {
            var i = 0
            val ss = SKMPEndpoint.bind(8888)
            do {
                var state = 0
                val callbacks = object : SimpleServerCallbacksServer {
                    override fun onReceiveRequestFromClient(v: Int) {
                        state = v
                    }

                    override fun onSendResponseToClient(): Int = state * 2
                }
                SimpleServerCallbackEndpointServer(callbacks).use {
                    it.accept(Client, ss)
                    it.start()
                }
                i++
            } while (true)
        }

        // Client
        repeat(20) { i ->
            launch {
                delay(Random.nextLong(0L, 100L))
                val callbacks = object : SimpleServerCallbacksClient {
                    override fun onReceiveResponseFromServer(v: Int) {
                        println("Client $i received $v")
                    }

                    override fun onSendRequestToServer() = i
                }
                SimpleServerCallbackEndpointClient(callbacks).use {
                    it.request(Server, "localhost", 8888)
                    it.start()
                }
            }
        }
    }
}