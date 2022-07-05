package app.impl

import adder.Client
import adder.Server
import adder.fluent.*
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
            // Server
            SKMPEndpoint().use { e ->
                e.connect(Client, chan)
                adderServer(e)
            }
        }
        launch {
            // Client
            SKMPEndpoint().use { e ->
                e.connect(Server, chan)
                adderClient(e)
            }
        }

    }
}

fun adderSockets() {
    val portChan = Channel<Int>()

    runBlocking {
        launch {
            // Client
            SKMPEndpoint().use { e ->
                e.request(Server, "localhost", portChan.receive())
                adderClient(e)
            }
        }
        launch {
            // Server
            SKMPEndpoint().use { e ->
                val s = SKMPEndpoint.bind()
                portChan.send(s.port)
                e.accept(Client, s)
                adderServer(e)
            }
        }

    }
}

suspend fun adderClient(e: SKMPEndpoint) {
    var number = 0

    var b: AdderClient1Interface = AdderClient1(e)
    repeat(adderIterations) {
        b = b
            .sendV1ToServer(number)
            .sendV2ToServer(number++)
            .receiveSumFromServer {}
    }
    b.sendQuitToServer()
}

suspend fun adderServer(e: SKMPEndpoint) {
    var cases: AdderServer1Branch? = AdderServer1(e).branch()

    while (cases != null) {

        var sum = 0

        cases = when (cases) {
            is AdderServer1_QuitInterface -> {
                cases.receiveQuitFromClient()
                null
            }
            is AdderServer1_V1Interface -> cases
                .receiveV1FromClient { sum = it }
                .receiveV2FromClient { sum += it }
                .sendSumToClient(sum)
                .branch()
        }
    }
}
