package sessionkotlin.dsl

import sessionkotlin.dsl.exception.InconsistentExternalChoiceException

class Examples {

    fun send() {
        val a = Role("A")
        val b = Role("B")
        val s = Role("C")

        globalProtocol {
            send<String>(a, s)
            send<Long>(s, a)
            send<Long>(s, b)
            send<Long>(b, s)
        }
    }

    fun choice() {
        val a = Role("A")
        val b = Role("B")

        globalProtocol {
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

    fun exec() {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")

        val case1 = globalProtocol {
            send<Int>(b, a)
            send<Int>(a, c)
        }

        val case2 = globalProtocol {
            send<String>(b, a)
        }

        globalProtocol {
            choice(b) {

                case("Case 1") {
                    exec(case1)
                }
                case("Case 2") {
                    exec(case2)
                }
            }
            
        }
    }

    fun rec() {
        val server = Role("Server")
        val client = Role("Client")

        globalProtocol {
            choice(client) {

                case("Add") {
                    send<Int>(client, server)
                    rec()
                }
                case("Result") {
                    send<String>(client, server)
                    send<Int>(server, client)
                }
            }

        }
    }
}
