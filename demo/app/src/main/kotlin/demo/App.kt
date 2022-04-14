package demo

import A
import B
import Simple_A_1
import Simple_B_1
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.david.sessionkotlin_lib.backend.SKBuffer
import org.david.sessionkotlin_lib.backend.SKMPEndpoint
import org.david.sessionkotlin_lib.backend.channel.SKChannel


//
//fun main() {
//    val chan = SKChannel(A, B)
//    runBlocking {
//        launch {
//            SKEndpoint().use { e ->
//                e.connect(B, chan)
//                val a1 = Proto1_A_1(e)
//                    .branch()
//                when (a1) {
//                    is Proto1_A_2_Case1 -> {
//                        a1.receiveFromB(SKBuffer())
//                            .branchOK()
//                            .sendToB("abc")
//                            .branchOK()
//                            .sendToB("def")
//                            .branchExit()
//                            .sendToB()
//                    }
//                    is Proto1_A_9_Case2 ->
//                        a1.receiveFromB(SKBuffer())
//                }
//            }
//        }
//
//        launch {
//            runBlocking {
//                SKEndpoint().use { e ->
//                    e.connect(A, chan)
//                    val b1 = Proto1_B_1(e)
//                        .branchCase1()
//                        .sendToA(10)
//                        .branch()
//
//                    do {
//                        when (b1) {
//                            is Proto1_B_4_OK -> b1
//                                .receiveFromA(SKBuffer())
//                                .branch()
//
//                            is Proto1_B_6_Exit -> {
//                                b1.receiveFromA()
//                                break
//                            }
//                        }
//                    } while (true)
//                }
//            }
//        }
//    }
//}

fun main() {
    val chan = SKChannel(A, B)
    runBlocking {
        launch {
            val buf = SKBuffer<Int>()
            SKMPEndpoint().use {
                it.connect(B, chan)
                Simple_A_1(it)
                    .receiveFromB(buf)
                    .sendToB((buf.value?:0) * 2)
            }
        }
        launch {
            val buf = SKBuffer<Int>()
            SKMPEndpoint().use {
                it.connect(A, chan)
                Simple_B_1(it)
                    .sendToA(12)
                    .receiveFromA(buf)
            }
            println(buf.value)
        }
    }
}