package demo

import A
import B
import C
import Simple_A_1
import Simple_B_1
import Simple_B_1_Interface
import Simple_B_2_1
import Simple_B_5_2
import Simple_C_1
import Simple_C_1_Interface
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
                    .sendToB(2)
                    .branch2()
                    .sendToB("bye!")
            }
        }
        launch {
            // B
            SKMPEndpoint().use { e ->
                e.connect(A, chanAB)
                e.accept(C, 9999)

                var b1: Simple_B_1_Interface = Simple_B_1(e)
                do {
                    when (val b2 = b1.branch()) {
                        is Simple_B_2_1 -> {
                            val buf = SKBuffer<Int>()
                            b1 = b2
                                .receiveFromA(buf)
                                .sendToC(buf.value * 2)
                        }
                        is Simple_B_5_2 -> {
                            val buf = SKBuffer<String>()
                            b2
                                .receiveFromA(buf)
                                .sendToC(buf.value + ", and hello from B")
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
                var b1: Simple_C_1_Interface = Simple_C_1(e)
                do {
                    when (val b2 = b1.branch()) {
                        is Simple_C_2_1 -> {
                            val bufInt = SKBuffer<Int>()
                            b1 = b2.receiveFromB(bufInt)
                            println("Received int: ${bufInt.value}")
                        }
                        is Simple_C_4_2 -> {
                            val bufString = SKBuffer<String>()
                            b2.receiveFromB(bufString)
                            println("Received string: ${bufString.value}")
                            break
                        }
                    }
                } while (true)
            }
        }
    }
}