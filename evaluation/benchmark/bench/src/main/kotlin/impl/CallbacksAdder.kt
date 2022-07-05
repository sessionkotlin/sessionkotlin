package app.impl

import adder.Client
import adder.Server
import adder.callbacks.*
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.endpoint.SKServerSocket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import impl.adderIterations


fun adderCallbacksChannels() {
    val chan = SKChannel(Server, Client)

    runBlocking {

        val job1 = launch {
            // Client
            AdderClientCallbacksEndpoint(adderClientCallbacks()).use { e ->
                e.connect(Server, chan)
                e.start()
            }
        }
        val job2 = launch {
            // Server
            AdderServerCallbacksEndpoint(adderServerCallbacks()).use { e ->
                e.connect(Client, chan)
                e.start()
            }
        }
        job1.join()
        job2.join()
    }
}

fun adderCallbacksSockets(serverSocket: SKServerSocket) {
    runBlocking {
        val portChan = Channel<Int>()

        val job1 = launch {
            // Client
            AdderClientCallbacksEndpoint(adderClientCallbacks()).use { e ->
                e.request(Server, "localhost", serverSocket.port)
                e.start()
            }
        }
        val job2 = launch {
            // Server
            AdderServerCallbacksEndpoint(adderServerCallbacks()).use { e ->
                e.accept(Client, serverSocket)
                e.start()
            }
        }
        job1.join()
        job2.join()
    }
}

fun adderClientCallbacks(): AdderClientCallbacks {
    var index = 0
    var number = 0
    return object : AdderClientCallbacks {
        override fun onChoice1() = if (index++ < adderIterations) Choice1.Choice1_V1 else Choice1.Choice1_Quit

        override fun sendQuitToServer() {}

        override fun sendV1ToServer() = number

        override fun sendV2ToServer(): Int = number++

        override fun receiveSumFromServer(v: Int) {}
    }
}

fun adderServerCallbacks(): AdderServerCallbacks {
    var sum = 0
    return object : AdderServerCallbacks {
        override fun receiveQuitFromClient(v: Unit) {}

        override fun receiveV1FromClient(v: Int) {
            sum = v
        }

        override fun receiveV2FromClient(v: Int) {
            sum += v
        }

        override fun sendSumToClient() = sum
    }
}
