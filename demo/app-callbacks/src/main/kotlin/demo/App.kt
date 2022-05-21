package demo

import A
import B
import C
import Choice1
import SimpleCallbacksA
import SimpleCallbacksB
import SimpleCallbacksC
import SimpleCallbacksClassA
import SimpleCallbacksClassB
import SimpleCallbacksClassC
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking


fun main() {
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