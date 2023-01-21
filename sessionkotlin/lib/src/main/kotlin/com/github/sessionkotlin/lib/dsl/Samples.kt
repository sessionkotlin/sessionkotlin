package com.github.sessionkotlin.lib.dsl

public class Samples {

    public fun send() {
        val a = SKRole("A")
        val b = SKRole("B")
        val s = SKRole("C")

        globalProtocol("ProtocolName") {
            send<String>(a, s)
            send<Long>(s, a)
            send<Long>(s, b)
            send<Long>(b, s)
        }
    }

    public fun sendTypes() {
        val a = SKRole("A")
        val b = SKRole("B")
        val s = SKRole("C")

        globalProtocol("ProtocolName") {
            send(a, s, String::class.java)
            send(s, a, Long::class.java)
            send(s, b, Long::class.java)
            send(b, s, Long::class.java)
        }
    }

    public fun choice() {
        val a = SKRole("A")
        val b = SKRole("B")

        globalProtocol("ProtocolName") {
            choice(b) {
                branch {
                    send<String>(b, a, "ok")
                }
                branch {
                    send<Long>(b, a, "quit")
                }
            }
        }
    }

    public fun exec() {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")

        fun case(z: SKRole, label: String): GlobalProtocol = {
            send<String>(a, b, label)
            send<String>(a, c, label)
            send<String>(z, a)
        }

        globalProtocol("ProtocolName") {
            choice(a) {
                branch {
                    case(b, "Case1")()
                }
                branch {
                    case(c, "Case2")()
                }
            }
        }
    }

    public fun goto() {
        val server = SKRole("Server")
        val client = SKRole("Client")

        globalProtocol("ProtocolName") {
            val t = mu()
            choice(client) {

                branch {
                    send<Int>(client, server, "Add")
                    goto(t)
                }
                branch {
                    send<String>(client, server, "Result")
                    send<Int>(server, client)
                }
            }
        }
    }
}
