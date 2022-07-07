package impl

import adder.Client
import adder.Server
import adder.fluent.*
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.endpoint.SKServerSocket
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun adderFluentChannels() {
    val chan = SKChannel(Client, Server)

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

fun adderFluentSockets(serverSocket: SKServerSocket) {
    runBlocking {
        launch {
            // Server
            SKMPEndpoint().use { e ->
                e.accept(Client, serverSocket)
                adderServer(e)
            }
        }
        launch {
            // Client
            SKMPEndpoint().use { e ->
                e.request(Server, "localhost", serverSocket.port)
                adderClient(e)
            }
        }
    }
}

suspend fun adderServer(e: SKMPEndpoint) {
    var cases: AdderServer1Branch? = AdderServer1(e).branch()
    var sum = 0

    while (cases != null) {
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
