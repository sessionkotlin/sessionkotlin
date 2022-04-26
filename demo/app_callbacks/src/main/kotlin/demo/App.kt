package demo

import A
import B
import C
import SimpleCallbacksClass_A
import SimpleCallbacksClass_B
import SimpleCallbacksClass_C
import SimpleCallbacks_A
import SimpleCallbacks_B
import SimpleCallbacks_C
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.david.sessionkotlin.backend.SKMPEndpoint
import org.david.sessionkotlin.backend.channel.SKChannel


fun main() {
    val chanAB = SKChannel(A, B)
//    val chanBC = SKChannel(B, C)

    runBlocking {
        launch {
            // A
            SKMPEndpoint().use {e ->
                e.connect(B, chanAB)
                val callbacks = object : SimpleCallbacks_A {
                    override fun onSendVal1ToB() {
                        TODO("Not yet implemented")
                    }

                    override fun onSendVal3ToB() {
                        TODO("Not yet implemented")
                    }

                }

                SimpleCallbacksClass_A(e, callbacks)
                    .start()
            }
        }
        launch {
            // B
            SKMPEndpoint().use { e ->
                e.connect(A, chanAB)
                e.accept(C, 9999)
                val callbacks = object : SimpleCallbacks_B {
                    override fun onSendVal2ToC() {
                        TODO("Not yet implemented")
                    }

                    override fun onReceiveVal1FromA() {
                        TODO("Not yet implemented")
                    }

                    override fun onSendVal4ToC() {
                        TODO("Not yet implemented")
                    }

                    override fun onReceiveVal3FromA() {
                        TODO("Not yet implemented")
                    }
                }

                SimpleCallbacksClass_B(e, callbacks)
                    .start()

            }
        }
        launch {
            // C
            SKMPEndpoint().use { e ->
                e.request(B, "localhost", 9999)

                val callbacks = object : SimpleCallbacks_C {
                    override fun onReceiveVal2FromB() {
                        TODO("Not yet implemented")
                    }

                    override fun onReceiveVal4FromB() {
                        TODO("Not yet implemented")
                    }
                }

                SimpleCallbacksClass_C(e, callbacks)
                    .start()
            }
        }
    }
}