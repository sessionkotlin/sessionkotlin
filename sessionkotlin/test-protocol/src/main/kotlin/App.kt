import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.globalProtocol

fun main() {
    val s = SKRole("Server")
    val c = SKRole("Client")

    globalProtocol("Simple", true) {
        send<Int>(s, c, "Initial", "Initial != 0")

        choice(s) {
            branch {
                send<Long>(s, c, "_250H")
            }
            branch {
                val t2 = mu()
                choice(s) {
                    branch {
                        send<Int>(s, c, "_201", "_201 > 0")
                        send<Int>(s, c, "_200")
                        goto(t2)
                    }
                    branch {
                        send<Int>(s, c, "_250")
                    }
                    branch {
                        send<Int>(s, c)
                    }
                }
            }
        }
    }
}

