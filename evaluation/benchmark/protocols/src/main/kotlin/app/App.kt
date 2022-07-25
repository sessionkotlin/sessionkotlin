package app

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol
import java.util.*

fun main() {
    adderServer()
    adderServerRefined()
    twoBuyer()
}

private fun adderServer() {
    val client = SKRole("Client")
    val server = SKRole("Server")
    globalProtocol("Adder", true) {
        val t = mu()
        choice(client) {
            branch {
                send<Unit>(client, server, "Quit")
            }
            branch {
                send<Int>(client, server, "V1")
                send<Int>(client, server, "V2")
                send<Int>(server, client, "Sum")
                goto(t)
            }
        }
    }
}

private fun adderServerRefined() {
    val client = SKRole("Client")
    val server = SKRole("Server")
    globalProtocol("AdderRefined", true) {
        val t = mu()
        choice(client) {
            branch {
                send<Unit>(client, server, "Quit")
            }
            branch {
                send<Int>(client, server, "V1")
                send<Int>(client, server, "V2")
                send<Int>(server, client, "Sum", "Sum == V1 + V2")
                goto(t)
            }
        }
    }
}

private fun twoBuyer() {
    val a = SKRole("ClientA")
    val b = SKRole("ClientB")
    val seller = SKRole("Seller")

    globalProtocol("TwoBuyer", true) {
        val t = mu()
        choice(a) {
            branch {
                send<String>(a, seller, "Id")

                send<Int>(seller, a, "Price")
                send<Int>(seller, b, "Price")

                send<Int>(a, b, "aShare")

                choice(b) {
                    branch {
                        send<String>(b, seller, "Address")
                        send<Date>(seller, b, "Date")
                        send<Date>(b, a, "Date")
                        goto(t)
                    }
                    branch {
                        send<Unit>(b, seller, "Reject")
                        send<Unit>(b, a, "Reject")
                        goto(t)
                    }
                }
            }
            branch {
                send<Unit>(a, seller, "Quit")
                send<Unit>(seller, b, "Quit")
            }
        }

    }
}

