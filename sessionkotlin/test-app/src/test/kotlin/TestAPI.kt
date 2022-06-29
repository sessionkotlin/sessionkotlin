import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import simple.A
import simple.B
import simple.C
import simple.callback.*
import simple.fluent.*

class TestAPI {
//
//    @Test
//    fun `test fluent`() {
//        val chanAB = SKChannel(A, B)
//        val c = Channel<Int>()
//
//        runBlocking {
//            launch {
//                // A
//                SKMPEndpoint().use {
//                    it.connect(B, chanAB)
//                    SimpleA1(it)
//                        .branch1()
//                        .sendToB(2)
//                        .branch3()
//                        .sendToB()
//                }
//            }
//            launch {
//                // B
//                SKMPEndpoint().use { e ->
//                    e.connect(A, chanAB)
//                    val s = SKMPEndpoint.bind()
//                    c.send(s.port)
//                    e.accept(C, s)
//
//                    var b1 = SimpleB1(e).receiveFromC()
//                    do {
//                        when (val b2 = b1.branch()) {
//                            is SimpleB3_1 -> {
//                                val buf = SKBuffer<Int>()
//                                b1 = b2
//                                    .receiveFromA(buf)
//                                    .sendToC(buf.value * 2)
//                            }
//                            is SimpleB6_2 -> {
//                                val buf = SKBuffer<Int>()
//                                b2
//                                    .receiveFromA(buf)
//                                    .sendToC(buf.value - 1)
//                                break
//                            }
//                            is SimpleB9_3 -> {
//                                b2.receiveFromA().sendToC()
//                                break
//                            }
//                        }
//                    } while (true)
//                }
//            }
//            launch {
//                // C
//                SKMPEndpoint().use { e ->
//                    e.request(B, "localhost", c.receive())
//                    var b1 = SimpleC1(e).sendToB()
//                    do {
//                        when (val b2 = b1.branch()) {
//                            is SimpleC3_1 -> {
//                                val bufInt = SKBuffer<Int>()
//                                b1 = b2.receiveFromB(bufInt)
//                                println("Received int: ${bufInt.value}")
//                            }
//                            is SimpleC5_2 -> {
//                                val bufString = SKBuffer<Int>()
//                                b2.receiveFromB(bufString)
//                                println("Received int 2: ${bufString.value}")
//                                break
//                            }
//                            is SimpleC7_3 -> {
//                                b2.receiveFromB()
//                                break
//                            }
//                        }
//                    } while (true)
//                }
//            }
//        }
//    }
//
//    @Test
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
