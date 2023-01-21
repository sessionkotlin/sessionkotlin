package readme

import com.github.sessionkotlin.lib.dsl.GlobalProtocol
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.globalProtocol
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
                    send<Address>(b, seller, "OK")
                    send<Date>(seller, b)
                    send<Date>(b, a, "OK")
                }
                branch {
                    send<Unit>(b, seller, "QUIT")
                    send<Unit>(b, a, "QUIT")
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
                    send<Int>(a, b, "1")
                    subProtocol(a, b)() // Proceed with subProtocol
                }
                branch {
                    send<Int>(a, b, "2")
                    subProtocol(b, a)() // Proceed with subProtocol, but with reversed roles
                }
            }
        }
    }
}

class Address
class Date
