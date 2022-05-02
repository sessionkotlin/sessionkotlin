package org.david.sessionkotlin.dsl

public class Samples {

    public fun send() {
        val a = SKRole("A")
        val b = SKRole("B")
        val s = SKRole("C")

        globalProtocolInternal {
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

        globalProtocolInternal {
            send(a, s, String::class.java)
            send(s, a, Long::class.java)
            send(s, b, Long::class.java)
            send(b, s, Long::class.java)
        }
    }

    public fun choice() {
        val a = SKRole("A")
        val b = SKRole("B")

        globalProtocolInternal {
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

        val z = SKRole("Z")

        val case1 = globalProtocolInternal {
            send<Int>(b, a)
            send<Int>(a, c)
        }

        val case2 = globalProtocolInternal {
            send<String>(b, a)
            send<String>(a, z)
        }

        globalProtocolInternal {
            choice(b) {

                branch("Case1") {
                    exec(case1)
                }
                branch("Case2") {
                    exec(case2, mapOf(z to c))
                }
            }
        }
    }

    public fun goto() {
        val server = SKRole("Server")
        val client = SKRole("Client")

        globalProtocolInternal {
            val t = miu()
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