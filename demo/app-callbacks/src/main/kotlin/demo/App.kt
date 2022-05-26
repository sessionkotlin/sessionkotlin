package demo

import A
import B
import C
import Choice1
import SimpleCallbackEndpointA
import SimpleCallbackEndpointB
import SimpleCallbackEndpointC
import SimpleCallbacksA
import SimpleCallbacksB
import SimpleCallbacksC

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
            SimpleCallbackEndpointA(callbacks).use { e ->
                e.connect(B, chanAB)
                e.start()
            }
        }
        launch {
            // B
            val callbacks = object : SimpleCallbacksB {
                var receivedInt = -1
                override fun onSendVal2ToC(): Int = receivedInt * 2
                override fun onReceiveVal1FromA(v: Int) {
                    receivedInt = v
                }

                override fun onSendVal4ToC(): Int = receivedInt - 1
                override fun onReceiveVal3FromA(v: Int) {
                    receivedInt = v
                }
            }
            SimpleCallbackEndpointB(callbacks).use { e ->
                e.connect(A, chanAB)
                e.accept(C, 9999)
                e.start()
            }
        }
        launch {
            // C
            val callbacks = object : SimpleCallbacksC {
                override fun onReceiveVal2FromB(v: Int) = println(v)
                override fun onReceiveVal4FromB(v: Int) = println(v)
            }
            SimpleCallbackEndpointC(callbacks).use { e ->
                e.request(B, "localhost", 9999)
                e.start()
            }
        }
    }
}