package demo

import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol


fun main() {
    val a = SKRole("A")
    val b = SKRole("B")
    val c = SKRole("C")

    val subProtocol: GlobalProtocol = {
        send<Int>(a, b, "val1")
        send<Int>(b, c, "val2", "val2 > val1")
    }

    globalProtocol("Simple", true) {
        val t = miu()
        choice(a) {
            branch("1") {
                subProtocol()
                goto(t)
            }
            branch("2") {
                send<Int>(a, b, "val3")
                send<Int>(b, c, "val4", "val4 < val3")
            }
        }
    }
}