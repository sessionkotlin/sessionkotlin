package com.github.d_costa.sessionkotlin.dsl

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
                branch("Ok") {
                    send<String>(b, a)
                }
                branch("Quit") {
                    send<Long>(b, a)
                }
            }
        }
    }

    public fun exec() {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")

        fun case(z: SKRole): GlobalProtocol = {
            send<String>(a, b)
            send<String>(a, c)
            send<String>(z, a)
        }

        globalProtocol("ProtocolName") {
            choice(a) {
                branch("Case1") {
                    case(b)()
                }
                branch("Case2") {
                    case(c)()
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

                branch("Add") {
                    send<Int>(client, server)
                    goto(t)
                }
                branch("Result") {
                    send<String>(client, server)
                    send<Int>(server, client)
                }
            }
        }
    }
}
