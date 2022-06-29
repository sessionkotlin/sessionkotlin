package demo


import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import simple_server.Client
import simple_server.Server
import simple_server.fluent.*
import kotlin.random.Random


fun main() {
    fluent()
}

fun fluent() {
    runBlocking {
        val chan = SKChannel()
        // Server
        launch {
            SKMPEndpoint().use {  e ->
                e.connect(Client, chan)
                SimpleServerServer1(e)
                    .branchCont()
                    .branchCont_Yes()
                    .sendToClient(10)
            }
        }

        // Client
        launch {
            SKMPEndpoint().use {e ->
                e.connect(Server, chan)

                val b1 = SimpleServerClient1(e)
                    .branch()
                when(b1) {

                    is SimpleServerClient2_Cont_Yes -> b1.receiveFromServer(SKBuffer())
                    is SimpleServerClient4_Cont_No -> b1.receiveFromServer(SKBuffer())
                    is SimpleServerClient6_Quit -> b1.receiveFromServer(SKBuffer())
                }
            }

        }
    }
}
