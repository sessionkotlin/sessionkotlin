package org.david.sessionkotlin_lib.dsl

public class Samples {

    public fun send() {
        val a = Role("A")
        val b = Role("B")
        val s = Role("C")

        globalProtocolInternal {
            send<String>(a, s)
            send<Long>(s, a)
            send<Long>(s, b)
            send<Long>(b, s)
        }
    }

    public fun sendTypes() {
        val a = Role("A")
        val b = Role("B")
        val s = Role("C")

        globalProtocolInternal {
            send(a, s, String::class.java)
            send(s, a, Long::class.java)
            send(s, b, Long::class.java)
            send(b, s, Long::class.java)
        }
    }

    public fun choice() {
        val a = Role("A")
        val b = Role("B")

        globalProtocolInternal {
            choice(b) {
                case("Ok") {
                    send<String>(b, a)
                }
                case("Quit") {
                    send<Long>(b, a)
                }
            }
        }
    }

    public fun exec() {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")

        val z = Role("Z")

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

                case("Case 1") {
                    exec(case1)
                }
                case("Case 2") {
                    exec(case2, mapOf(z to c))
                }
            }
        }
    }

    public fun goto() {
        val server = Role("Server")
        val client = Role("Client")

        globalProtocolInternal {
            val t = miu("X")
            choice(client) {

                case("Add") {
                    send<Int>(client, server)
                    goto(t)
                }
                case("Result") {
                    send<String>(client, server)
                    send<Int>(server, client)
                }
            }
        }
    }
}
