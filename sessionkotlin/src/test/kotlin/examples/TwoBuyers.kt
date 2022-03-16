package examples

import org.junit.jupiter.api.Test
import sessionkotlin.dsl.Role
import sessionkotlin.dsl.globalProtocol
import java.util.*

class TwoBuyers {

    @Test
    fun main() {
        val a = Role("Client A")
        val b = Role("Client B")
        val seller = Role("Seller")

        val aux = globalProtocol {
            choice(b) {
                case("Ok") {
                    send<Address>(b, seller)
                    send<Date>(seller, b)
                    send<Date>(b, a)
                }
                case("Quit") {
                    send<Unit>(b, seller)
                    send<Unit>(b, a)
                }
            }
        }

        globalProtocol {
            send<String>(a, seller)

            send<Int>(seller, a)
            send<Int>(seller, b)

            send<Int>(a, b)

            exec(aux)
        }

    }

    class Address(
        val postalCode: String,
        val street: String,
    )
}