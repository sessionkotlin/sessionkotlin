package demo

import A
import B
import C
import SimpleA1
import SimpleB1
import SimpleB1_Interface
import SimpleB2_1
import SimpleB5_2
import SimpleC1
import SimpleC1_Interface
import SimpleC2_1
import SimpleC4_2
import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    val chanAB = SKChannel(A, B)
//    val chanBC = SKChannel(B, C)

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