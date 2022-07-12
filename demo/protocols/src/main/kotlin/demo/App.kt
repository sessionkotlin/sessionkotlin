package demo

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol

fun main() {
    val s = SKRole("Server")
    val c = SKRole("Client")

    globalProtocol("Simple") {
        choice(s) {
            branch {
                val t = mu()
                choice(s) {
                    branch {
                        send<Long>(s, c, "250H")
                        goto(t)
                    }
                    branch {
                        send<Long>(s, c, "250")
                    }
                }
            }
            branch {
                send<Int>(s, c, "Quit")
            }
        }
    }
}
