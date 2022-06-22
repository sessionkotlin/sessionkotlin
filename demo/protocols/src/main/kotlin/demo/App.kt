package demo

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol

fun main() {
    val s = SKRole("Server")
    val c = SKRole("Client")

    globalProtocol("Simple Server", true) {
        send<Unit>(c, s, "dummy")
        send<Int>(c, s, "request")
        send<Int>(s, c, "response")
    }
}
