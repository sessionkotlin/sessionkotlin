package demo

import A
import B
import C
import SimpleCallbacksA
import SimpleCallbacksB
import SimpleCallbacksC
import SimpleCallbacksClassA
import SimpleCallbacksClassB
import SimpleCallbacksClassC
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.david.sessionkotlin.backend.channel.SKChannel


fun main() {
    val chanAB = SKChannel(A, B)

    runBlocking {
        launch {
            // A
            val callbacks = object : SimpleCallbacksA {
                override fun onSendVal1ToB(): Int = 10
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
                override fun onSendVal2ToC(): Int = receivedInt + 1
                override fun onReceiveVal1FromA(value: Int) {
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
            }
            SimpleCallbacksClassC(callbacks).use { e ->
                e.request(B, "localhost", 9999)
                e.start()
            }
        }
    }
}