package demo

import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.globalProtocol


fun main() {
    val a = SKRole("A")
    val b = SKRole("B")
    val c = SKRole("C")

    globalProtocol("Simple", true) {
        send<Int>(a, b, "val1")
        send<Int>(b, c, "val2", "val2 > val1")
    }
}