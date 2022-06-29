package dsl.examples

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import org.junit.jupiter.api.Test
import java.util.*

class TwoBuyersRefined {

    @Test
    fun main() {
        val a = SKRole("ClientA")
        val b = SKRole("ClientB")
        val seller = SKRole("Seller")

        globalProtocolInternal {
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

    class Address
}
