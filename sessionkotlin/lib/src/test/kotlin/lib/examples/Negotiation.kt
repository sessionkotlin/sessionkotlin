package lib.examples

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test

class Negotiation {

    @Test
    fun main() {
        val buyer = Role("Buyer")
        val seller = Role("Seller")

        globalProtocol {
            send<Int>(buyer, seller)
            val t = miu("X")

            choice(seller) {
                case("Accept1") {
                    send<Unit>(seller, buyer)
                    send<Unit>(buyer, seller)
                }
                case("Reject1") {
                    send<Unit>(seller, buyer)
                }
                case("Haggle1") {
                    send<Int>(seller, buyer)
                    choice(buyer) {
                        case("Accept2") {
                            send<Unit>(buyer, seller)
                            send<Unit>(seller, buyer)
                        }
                        case("Reject2") {
                            send<Unit>(buyer, seller)
                        }
                        case("Haggle2") {
                            send<Int>(buyer, seller)
                            goto(t)
                        }
                    }
                }
            }
        }.dump()
    }
}
