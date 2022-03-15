package examples

import org.junit.jupiter.api.Test
import sessionkotlin.dsl.Role
import sessionkotlin.dsl.globalProtocol

class AdderExample {

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