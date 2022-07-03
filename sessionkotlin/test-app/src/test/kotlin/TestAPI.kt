import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import simple.Client
import simple.Server
import simple.callbacks.*

import simple.fluent.*

class TestAPI {

    @Test
    fun `test fluent`() {
        val c = Channel<Int>()

        runBlocking {
            launch {
                suspend fun handle201(initialState: SimpleClient7Interface) {
                    var state = initialState

                    while (true) {
                        val b = state.receive_200FromServer(SKBuffer())
                            .branch()
                        when (b) {
                            is SimpleClient8_Interface -> {
                                b.receiveFromServer(SKBuffer())
                                break
                            }
                            is SimpleClient8__201Interface -> state = b.receive_201FromServer(SKBuffer())
                            is SimpleClient8__250Interface -> {
                                b.receive_250FromServer(SKBuffer())
                                break
                            }
                        }
                    }
                }

                // Client
                SKMPEndpoint().use { e ->
                    e.request(Server, "localhost", c.receive())
                    SimpleClient1(e)
                        .receiveInitialFromServer(SKBuffer())
                        .branch()
                        .let { s ->
                            when (s) {
                                is SimpleClient2_Interface -> s.receiveFromServer(SKBuffer())
                                is SimpleClient2__201Interface -> handle201(s.receive_201FromServer(SKBuffer()))
                                is SimpleClient2__250Interface -> s.receive_250FromServer(SKBuffer())
                                is SimpleClient2__250HInterface -> s.receive_250HFromServer(SKBuffer())
                            }
                        }
                }


            }
            launch {
                // Server
                SKMPEndpoint().use { e ->
                    val ss = SKMPEndpoint.bind()
                    c.send(ss.port)
                    e.accept(Client, ss)
                    SimpleServer1(e)
                        .sendInitialToClient(10)
                        .send_201ToClient(1)
                        .send_200ToClient(10)
                        .sendToClient(11)
                }
            }

        }
    }

    @Test
    fun `test callbacks`() {
        val chanAB = SKChannel()

        runBlocking {
            launch {
                // A
                val callbacks = object : SimpleClientCallbacks {
                    override fun receiveInitialFromServer(v: Int) {}
                    override fun receive_250HFromServer(v: Long) {}
                    override fun receive_201FromServer(v: Int) {}
                    override fun receive_250FromServer(v: Int) {}
                    override fun receiveFromServer(v: Int) {}
                    override fun receive_200FromServer(v: Int) {}
                }
                SimpleClientCallbacksEndpoint(callbacks).use { e ->
                    e.connect(Server, chanAB)
                    e.start()
                }
            }
            launch {
                // B
                val callbacks = object : SimpleServerCallbacks {
                    override fun sendInitialToClient(): Int = 1
                    override fun onChoice2(): Choice2 = Choice2.Choice2__201
                    override fun send_250HToClient(): Long = 10L
                    override fun send_201ToClient(): Int = 2
                    override fun send_250ToClient(): Int = 1
                    override fun sendToClient(): Int = 10
                    override fun send_200ToClient(): Int = 10
                    override fun onChoice8(): Choice8 = Choice8.Choice8__250
                }
                SimpleServerCallbacksEndpoint(callbacks).use { e ->
                    e.connect(Client, chanAB)
                    e.start()
                }
            }
        }
    }
}
