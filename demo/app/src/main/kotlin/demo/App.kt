package demo


import Proto1A1
import Proto1A2
import org.david.sessionkotlin_lib.api.SKBuffer
import org.david.sessionkotlin_lib.dsl.*
import java.io.File

fun main() {
    val a = Role("A")
    val b = Role("B")

    globalProtocol {
        send<Int>(a, b)
        val t1 = miu("X")
        send<String>(b, a)
        miu("Y")
        send<Long>(b, a)
        goto(t1)
    }

    val buf = SKBuffer<Int>()

//    Proto1A1()
//        .sendToB(10)
//        .receiveFromB(buf)
//
//    Proto1A2()

}
