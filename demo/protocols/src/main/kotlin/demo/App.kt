package demo

import org.david.sessionkotlin_lib.dsl.*

fun main() {
    val a = SKRole("A")
    val b = SKRole("B")
    val c = SKRole("C")

    globalProtocol("Simple") {
        val t = miu()
        choice(a) {
            case("1") {
                send<Int>(a, b)
                send<Int>(b, c)
                goto(t)
            }
            case("2") {
                send<String>(a, b)
                send<String>(b, c)
            }
        }
    }
}