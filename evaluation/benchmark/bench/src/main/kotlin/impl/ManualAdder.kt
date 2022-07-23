package impl

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


fun adderManualSockets(serverSocket: ServerSocket, selector: SelectorManager) {
    runBlocking {
        launch {
            // Client
            val s = aSocket(selector).tcp().connect("localhost", serverSocket.localAddress.toJavaAddress().port)

            val o = s.openWriteChannel(autoFlush = true)
            val i = s.openReadChannel()

            var number = 0
            repeat(adderIterations) {
                o.writeInt(number)
                o.writeInt(number++)
                i.readInt()
            }

            withContext(Dispatchers.IO) {
                s.close()
            }
        }
        launch {
            // Server
            val s = serverSocket.accept()

            val o = s.openWriteChannel(autoFlush = true)
            val i = s.openReadChannel()

            var sum: Int
            repeat(adderIterations) {
                sum = i.readInt()
                sum += i.readInt()
                o.writeInt(sum)
            }
            withContext(Dispatchers.IO) {
                s.close()
            }
        }
    }
}


fun adderManualChannels() {
    runBlocking {
        val chanClient = Channel<Int>() // client listens here
        val chanServer = Channel<Int>() // server listens here

        launch {
            // Client
            var index = 0
            var number = 0

            while(index++ < adderIterations) {
                chanServer.send(number)
                chanServer.send(number++)
                chanClient.receive()
            }
        }
        launch {
            // Server
            var sum: Int
            var index = 0

            while(index++ < adderIterations) {
                sum = chanServer.receive()
                sum += chanServer.receive()
                chanClient.send(sum)
            }
        }
    }
}
