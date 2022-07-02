import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import simple.Client
import simple.Server

import simple.fluent.*

class TestAPI {

    @Test
    fun `test fluent`() {
        val c = Channel<Int>()

        runBlocking {
            launch {
                suspend fun handle201(initialState: SimpleClient7) {
                    var state = initialState

                    while (true) {
                        val b = state.receive200FromServer(SKBuffer())
                            .branch()
                        when (b) {
                            is SimpleClient8_ -> {
                                b.receiveFromServer(SKBuffer())
                                break
                            }
                            is SimpleClient8_201 -> state = b.receive201FromServer(SKBuffer())
                            is SimpleClient8_250 -> {
                                b.receive250FromServer(SKBuffer())
                                break
                            }
                        }
                    }
                }

                // Client
                SKMPEndpoint().use { e ->
                    e.request(Server, "localhost", c.receive())
                    SimpleClient1(e)
                        .receiveFromServer(SKBuffer())
                        .branch()
                        .let { s ->
                            when (s) {
                                is SimpleClient2_ -> s.receiveFromServer(SKBuffer())
                                is SimpleClient2_201 -> handle201(s.receive201FromServer(SKBuffer()))
                                is SimpleClient2_250 -> s.receive250FromServer(SKBuffer())
                                is SimpleClient2_250H -> s.receive250HFromServer(SKBuffer())
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
                        .sendToClient(10)
                        .send201ToClient(1)
                        .send200ToClient(10)
                        .sendToClient(11)
                }
            }

        }
    }

//    @Test TODO implement
//    fun `test callbacks`() {
//        val chanAB = SKChannel(A, B)
//
//        runBlocking {
//            launch {
//                // A
//                val callbacks = object : SimpleCallbacksA {
//                    var index = 0
//                    override fun onChoose1(): Choice1 =
//                        if (index++ < 1) Choice1.Choice1_1
//                        else Choice1.Choice1_2
//
//                    override fun onSendVal1ToB(): Int = 1
//                    override fun onSendVal3ToB(): Int = 3
//                    override fun onSendD1ToB() {}
//                }
//                SimpleCallbackEndpointA(callbacks).use { e ->
//                    e.connect(B, chanAB)
//                    e.start()
//                }
//            }
//            launch {
//                // B
//                val callbacks = object : SimpleCallbacksB {
//                    var receivedInt = -1
//                    override fun onSendVal2ToC(): Int = receivedInt * 2
//                    override fun onReceiveVal1FromA(v: Int) {
//                        receivedInt = v
//                    }
//
//                    override fun onSendVal4ToC(): Int = receivedInt - 1
//                    override fun onReceiveVal3FromA(v: Int) {
//                        receivedInt = v
//                    }
//
//                    override fun onSendD2ToC() { }
//
//                    override fun onReceiveD1FromA() { }
//
//                    override fun onReceiveDummyFromC() { }
//                }
//                SimpleCallbackEndpointB(callbacks).use { e ->
//                    e.connect(A, chanAB)
//                    e.accept(C, 9999)
//                    e.start()
//                }
//            }
//            launch {
//                // C
//                val callbacks = object : SimpleCallbacksC {
//                    override fun onReceiveVal2FromB(v: Int) = println(v)
//                    override fun onReceiveVal4FromB(v: Int) = println(v)
//                    override fun onReceiveD2FromB() { }
//
//                    override fun onSendDummyToB() { }
//                }
//                SimpleCallbackEndpointC(callbacks).use { e ->
//                    e.request(B, "localhost", 9999)
//                    e.start()
//                }
//            }
//        }
//    }
}
