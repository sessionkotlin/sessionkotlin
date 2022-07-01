package readme

import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol
import org.junit.jupiter.api.Test

class ExampleTest {

    @Test
    fun `example 1`() {
        val a = SKRole("ClientA")
        val b = SKRole("ClientB")
        val seller = SKRole("Seller")

        globalProtocol("Protocol") {
            send<String>(a, seller)

            send<Int>(seller, a, "valA")
            send<Int>(seller, b, "valB", "valA == valB")

            send<Int>(a, b, "proposal", "proposal <= valA")

            choice(b) {
                branch {
                    send<Address>(b, seller)
                    send<Date>(seller, b)
                    send<Date>(b, a)
                }
                branch {
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
                branch {
                    send<Int>(a, b)
                    subProtocol(a, b)() // Proceed with subProtocol
                }
                branch {
                    send<Int>(a, b)
                    subProtocol(b, a)() // Proceed with subProtocol, but with reversed roles
                }
            }
        }
    }
}

class Address
class Date
