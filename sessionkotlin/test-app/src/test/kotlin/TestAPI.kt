import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class TestAPI {

    @Test
    fun `test fluent`() {
        val chanAB = SKChannel(A, B)

        runBlocking {
            launch {
                // A
                SKMPEndpoint().use {
                    it.connect(B, chanAB)
                    SimpleA1(it)
                        .branch1()
                        .sendToB(2)
                        .branch2()
                        .sendToB(0)
                }
            }
            launch {
                // B
                SKMPEndpoint().use { e ->
                    e.connect(A, chanAB)
                    e.accept(C, 9999)

                    var b1: SimpleB1_Interface = SimpleB1(e)
                    do {
                        when (val b2 = b1.branch()) {
                            is SimpleB2_1 -> {
                                val buf = SKBuffer<Int>()
                                b1 = b2
                                    .receiveFromA(buf)
                                    .sendToC(buf.value * 2)
                            }
                            is SimpleB5_2 -> {
                                val buf = SKBuffer<Int>()
                                b2
                                    .receiveFromA(buf)
                                    .sendToC(buf.value - 1)
                                break
                            }
                        }
                    } while (true)
                }
            }
            launch {
                // C
                SKMPEndpoint().use { e ->
                    e.request(B, "localhost", 9999)
                    var b1: SimpleC1_Interface = SimpleC1(e)
                    do {
                        when (val b2 = b1.branch()) {
                            is SimpleC2_1 -> {
                                val bufInt = SKBuffer<Int>()
                                b1 = b2.receiveFromB(bufInt)
                                println("Received int: ${bufInt.value}")
                            }
                            is SimpleC4_2 -> {
                                val bufString = SKBuffer<Int>()
                                b2.receiveFromB(bufString)
                                println("Received int 2: ${bufString.value}")
                                break
                            }
                        }
                    } while (true)
                }
            }
        }
    }

    fun `test callbacks`() {
        val chanAB = SKChannel(A, B)

        runBlocking {
            launch {
                // A
                val callbacks = object : SimpleCallbacksA {
                    var index = 0
                    override fun onChoose1(): Choice1 =
                        if (index++ < 1) Choice1.Choice1_1
                        else Choice1.Choice1_2
                    override fun onSendVal1ToB(): Int = 1
                    override fun onSendVal3ToB(): Int = 3
                }
                SimpleCallbacksClassA(callbacks).use { e ->
                    e.connect(B, chanAB)
                    e.start()
                }
            }
            launch {
                // B
                val callbacks = object : SimpleCallbacksB {
                    var receivedInt = -1
                    override fun onSendVal2ToC(): Int = receivedInt * 2
                    override fun onReceiveVal1FromA(value: Int) {
                        receivedInt = value
                    }

                    override fun onSendVal4ToC(): Int = receivedInt - 1
                    override fun onReceiveVal3FromA(value: Int) {
                        receivedInt = value
                    }
                }
                SimpleCallbacksClassB(callbacks).use { e ->
                    e.connect(A, chanAB)
                    e.accept(C, 9999)
                    e.start()
                }
            }
            launch {
                // C
                val callbacks = object : SimpleCallbacksC {
                    override fun onReceiveVal2FromB(value: Int) = println(value)
                    override fun onReceiveVal4FromB(value: Int) = println(value)
                }
                SimpleCallbacksClassC(callbacks).use { e ->
                    e.request(B, "localhost", 9999)
                    e.start()
                }
            }
        }
    }
}
