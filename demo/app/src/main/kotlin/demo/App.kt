package demo

import A
import B
import C
import Simple_A_1
import Simple_B_1
import Simple_B_2_1
import Simple_B_5_2
import Simple_C_1
import Simple_C_2_1
import Simple_C_4_2
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.david.sessionkotlin_lib.backend.SKBuffer
import org.david.sessionkotlin_lib.backend.SKMPEndpoint
import org.david.sessionkotlin_lib.backend.channel.SKChannel


fun main() {
    val chanAB = SKChannel(A, B)
//    val chanBC = SKChannel(B, C)

    runBlocking {
        launch {
            // A
            SKMPEndpoint().use {
                it.connect(B, chanAB)
                Simple_A_1(it)
                    .branch2()
                    .sendToB("helloo")
            }
        }
        launch {
            // B
            SKMPEndpoint().use { e ->
                e.connect(A, chanAB)
//                it.connect(C, chanBC)
                e.accept(C, 9999)
                val b = Simple_B_1(e)
                    .branch()
                when (b) {
                    is Simple_B_2_1 -> b.let {
                        val buf = SKBuffer<Int>()
                        it
                            .receiveFromA(buf)
                            .sendToC(buf.value * 2)
                    }
                    is Simple_B_5_2 -> b.let {
                        val buf = SKBuffer<String>()
                        it
                            .receiveFromA(buf)
                            .sendToC(buf.value + ", and hello from B")
                    }
                }
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