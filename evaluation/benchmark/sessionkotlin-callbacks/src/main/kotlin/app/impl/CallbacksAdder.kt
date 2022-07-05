package app.impl

import adder.Client
import adder.Server
import adder.callbacks.*
import adderIterations
import channelsKey
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import socketsKey


fun adder(backend: String) {
    when (backend) {
        channelsKey -> {
            adderChannels()
        }
        socketsKey -> {
            adderSockets()
        }
        else -> throw RuntimeException()
    }
}

fun adderChannels() {
    val chan = SKChannel()
    
    runBlocking {
        launch {
            // Client
            AdderClientCallbacksEndpoint(adderClientCallbacks()).use { e ->
                e.connect(Server, chan)
                e.start()
            }
        }
        launch {
            // Server
            AdderServerCallbacksEndpoint(adderServerCallbacks()).use { e ->
                e.connect(Client, chan)
                e.start()
            }
        }
    }
}

fun adderSockets() {
    val portChan = Channel<Int>()

    runBlocking {
        launch {
            // Client
            AdderClientCallbacksEndpoint(adderClientCallbacks()).use { e ->
                e.request(Server, "localhost", portChan.receive())
                e.start()
            }
        }
        launch {
            // Server
            AdderServerCallbacksEndpoint(adderServerCallbacks()).use { e ->
                val s = SKMPEndpoint.bind()
                portChan.send(s.port)
                e.accept(Client, s)
                e.start()
            }
        }
    }
}

fun adderClientCallbacks(): AdderClientCallbacks {
    var index = 0
    var number = 0
    return object : AdderClientCallbacks {
        override fun onChoice1() = if (index++ < adderIterations) Choice1.Choice1_V1 else Choice1.Choice1_Quit

        override fun sendQuitToServer() { }

        override fun sendV1ToServer() = number

        override fun sendV2ToServer(): Int = number++

        override fun receiveSumFromServer(v: Int) { }
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
