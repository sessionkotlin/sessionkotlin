package demo

import A
import B
import C
import SimpleCallbacksClass_B
import SimpleCallbacks_B
import Simple_A_1
import Simple_C_1
import Simple_C_2_1
import Simple_C_4_2
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.david.sessionkotlin.backend.SKBuffer
import org.david.sessionkotlin.backend.SKMPEndpoint
import org.david.sessionkotlin.backend.channel.SKChannel


fun main() {
    val chanAB = SKChannel(A, B)
//    val chanBC = SKChannel(B, C)

    runBlocking {
        launch {
            // A
            SKMPEndpoint().use {
                it.connect(B, chanAB)
                Simple_A_1(it)
                    .branch1()
                    .sendToB(10)
            }
        }
        launch {
            // B
            SKMPEndpoint().use { e ->
                e.connect(A, chanAB)
                e.accept(C, 9999)
                val callbacks = object : SimpleCallbacks_B {
                    var receivedInt = -1
                    var receivedString = ""

                    override fun onReceiveVal1FromA(value: Int) {
                        receivedInt = value
                    }

                    override fun onSendVal2ToC(): Int = receivedInt + 2

                    override fun onReceiveVal3FromA(value: String) {
                        receivedString = value
                    }

                    override fun onSendVal4ToC(): String = "$receivedString. Hello from B"
                }
                SimpleCallbacksClass_B(e, callbacks)
                    .start()

            }
        }
        launch {
            // C
            SKMPEndpoint().use { e ->
                e.request(B, "localhost", 9999)
//                it.connect(B, chanBC)

                val b = Simple_C_1(e)
                    .branch()
                when (b) {
                    is Simple_C_2_1 -> b.let {
                        val bufInt = SKBuffer<Int>()
                        it.receiveFromB(bufInt)
                        println("Received int: ${bufInt.value}")
                    }
                    is Simple_C_4_2 -> b
                        .let {
                            val bufString = SKBuffer<String>()
                            it.receiveFromB(bufString)
                            println("Received string: ${bufString.value}")
                        }
                }
            }
        }
    }
}