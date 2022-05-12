package readme

import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol
import org.junit.jupiter.api.Test

class ExampleTest {

    @Test
    fun `example 1`() {
        val a = SKRole("Client A")
        val b = SKRole("Client B")
        val seller = SKRole("Seller")

        globalProtocol("Protocol") {
            send<String>(a, seller)

            send<Int>(seller, a, "valSentToA")
            send<Int>(seller, b, "valSentToB", "valSentToA == valSentToB")

            send<Int>(a, b, "proposal", "proposal <= valSentToA")

            choice(b) {
                branch("Ok") {
                    send<Address>(b, seller)
                    send<Date>(seller, b)
                    send<Date>(b, a)
                }
                branch("Quit") {
                    send<Unit>(b, seller)
                    send<Unit>(b, a)
                }
            }
        }
    }

    @Test
    fun `example 2`() {
        val a = SKRole("A")
        val b = SKRole("B")

        fun subProtocol(x: SKRole, y: SKRole): GlobalProtocol = {
            send<Int>(x, y)
            send<Int>(y, x)
        }

        globalProtocol("Protocol 2") {
            choice(a) {
                branch("Branch1") {
                    send<Int>(a, b)
                    subProtocol(a, b)() // Proceed with subProtocol
                }
                branch("Branch2") {
                    send<Int>(a, b)
                    subProtocol(b, a)() // Proceed with subProtocol, but with reversed roles
                }
            }
        }
    }
}

class Address
class Date
