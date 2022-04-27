package demo

import A
import B
import C
import Choice1
import SimpleCallbacksClass_A
import SimpleCallbacksClass_B
import SimpleCallbacksClass_C
import SimpleCallbacks_A
import SimpleCallbacks_B
import SimpleCallbacks_C
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
            var index = 0
            val callbacks = object : SimpleCallbacks_A {
                override fun onChoose1(): Choice1 =
                    if (index++ < 2) Choice1.Choice1_1 else Choice1.Choice1_2
                override fun onSendVal1ToB(): Int = 10
                override fun onSendVal3ToB(): String = "something"
            }
            SimpleCallbacksClass_A(callbacks).use { e ->
                e.connect(B, chanAB)
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
            val callbacks = object : SimpleCallbacks_C {
                override fun onReceiveVal2FromB(value: Int) = println(value)
                override fun onReceiveVal4FromB(value: String) = println(value)
            }
            SimpleCallbacksClass_C(callbacks).use { e ->
                e.request(B, "localhost", 9999)
                e.start()
            }
        }
    }
}