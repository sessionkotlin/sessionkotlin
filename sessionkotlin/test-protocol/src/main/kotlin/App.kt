import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol

fun main() {
    val s = SKRole("Server")
    val c = SKRole("Client")

    globalProtocol("Simple") {
        choice(s) {
            branch {
                send<Long>(s, c, "250H")
            }
            branch {
                val t = mu()
                choice(s) {
                    branch {
                        send<Int>(s, c, "200")
                        goto(t)
                    }
                    branch {
                        send<Int>(s, c, "250")
                    }
                }
            }
        }
    }
}

