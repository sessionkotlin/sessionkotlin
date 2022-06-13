package demo

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol
import java.util.*

fun main() {
    val s = SKRole("Server")
    val c = SKRole("Client")

    globalProtocol("SimpleServer", true) {
        send<Int>(c, s, "request")
        send<Int>(s, c, "response")
    }
}
