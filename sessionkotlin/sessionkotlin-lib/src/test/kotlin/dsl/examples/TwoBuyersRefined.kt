package dsl.examples

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import org.junit.jupiter.api.Test
import java.util.*

class TwoBuyersRefined {

    @Test
    fun main() {
        val a = SKRole("Client A")
        val b = SKRole("Client B")
        val seller = SKRole("Seller")

        globalProtocolInternal {
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

    class Address
}
