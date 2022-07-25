package impl

import adderrefined.Client
import adderrefined.Server
import adderrefined.fluent.*
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.endpoint.SKServerSocket
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun adderRefinedFluentChannels() {
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

fun adderRefinedFluentSockets(serverSocket: SKServerSocket) {
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

private suspend fun adderServer(e: SKMPEndpoint) {
    var cases: AdderRefinedServer1Branch? = AdderRefinedServer1(e).branch()
    var sum = 0

    while (cases != null) {
        cases = when (cases) {
            is AdderRefinedServer1_QuitInterface -> {
                cases.receiveQuitFromClient()
                null
            }
            is AdderRefinedServer1_V1Interface -> cases
                .receiveV1FromClient { sum = it }
                .receiveV2FromClient { sum += it }
                .sendSumToClient(sum)
                .branch()
        }
    }
}

private suspend fun adderClient(e: SKMPEndpoint) {
    var number = 0

    var b: AdderRefinedClient1Interface = AdderRefinedClient1(e)
    repeat(adderIterations) {
        b = b
            .sendV1ToServer(number)
            .sendV2ToServer(number++)
            .receiveSumFromServer {}
    }
    b.sendQuitToServer()
}
