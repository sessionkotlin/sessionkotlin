package demo

import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.globalProtocol


fun main() {
    val a = SKRole("A")
    val b = SKRole("B")
    val c = SKRole("C")

    globalProtocol("Simple") {
        val t = miu()
        choice(a) {
            case("1") {
                send<Unit>(a, b, "val1")
                send<Int>(b, c, "val2")
                goto(t)
            }
            case("2") {
                send<String>(a, b, "val3")
                send<String>(b, c, "val4")
            }
        }
    }
}