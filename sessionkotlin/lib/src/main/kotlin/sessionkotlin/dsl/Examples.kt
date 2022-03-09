package sessionkotlin.dsl

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
}
