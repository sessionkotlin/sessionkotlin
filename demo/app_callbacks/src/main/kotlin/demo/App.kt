package demo

import A
import B
import C
import Choice1
import SimpleCallbacksClass_A
import SimpleCallbacksClass_B
import SimpleCallbacks_A
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

    runBlocking {
        launch {
            // A
            val callbacks = object : SimpleCallbacks_A {
                override fun onChoose1(): Choice1 = Choice1.Choice1_1
                override fun onSendVal1ToB(): Int = 10
                override fun onSendVal3ToB(): String {
                    TODO()
                }
            }
            SimpleCallbacksClass_A(callbacks).use { e ->
                e.connect(A, chanAB)
                e.accept(C, 9999)
                e.start()
            }
        }
        launch {
            // B
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
            SimpleCallbacksClass_B(callbacks).use { e ->
                e.connect(A, chanAB)
                e.accept(C, 9999)
                e.start()
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