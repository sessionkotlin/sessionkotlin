package dsl.examples

import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.globalProtocolInternal
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
                    send<Address>(b, seller, "address")
                    send<Date>(seller, b, "date")
                    send<Date>(b, a, "date")
                }
                branch {
                    send<Unit>(b, seller, "QUIT")
                    send<Unit>(b, a, "QUIT")
                }
            }
        }
    }

    class Address
}
