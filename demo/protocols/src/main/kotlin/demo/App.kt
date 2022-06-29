package demo

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol

fun main() {
    val s = SKRole("Server")
    val c = SKRole("Client")

    globalProtocol("Simple Server") {
        choice(s) {
            branch("Cont") {
                choice(s) {
                    branch("Cont_Yes") {
                        send<Long>(s, c)
                    }
                    branch("Cont_No") {
                        send<Long>(s, c)
                    }
                }
            }
            branch("Quit") {
                send<Int>(s, c)
            }
        }
    }
}
