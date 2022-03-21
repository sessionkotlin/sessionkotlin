package examples

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test

class Adder {

    @Test
    fun main() {
        val c = Role("Client")
        val s = Role("Server")

        globalProtocol {
            choice(c) {
                case("Continue") {
                    send<Int>(c, s)
                    send<Int>(c, s)
                    send<Int>(s, c)
                    rec()
                }
                case("Quit") {
                    send<Unit>(c, s)
                }
            }
        }
    }
}