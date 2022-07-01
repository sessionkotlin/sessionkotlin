import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol

fun main() {
    val s = SKRole("Server")
    val c = SKRole("Client")

    globalProtocol("Simple") {
        send<Int>(s, c)

        choice(s) {
            branch {
                send<Long>(s, c, "250H")
            }
            branch {
                val t2 = mu()
                choice(s) {
                    branch {
                        send<Int>(s, c, "201")
                        send<Int>(s, c, "200")
                        goto(t2)
                    }
                    branch {
                        send<Int>(s, c, "250")
                    }
                    branch {
                        send<Int>(s, c)
                    }
                }
            }
        }
    }
}

