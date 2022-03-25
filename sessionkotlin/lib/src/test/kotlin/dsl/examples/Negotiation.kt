package examples

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test

class Negotiation {

    @Test
    fun main() {
        val buyer = Role("Buyer")
        val seller = Role("Seller")

        val aux = globalProtocol {
            choice(seller) {
                case("Accept1") {
                    send<Unit>(seller, buyer)
                    rec()
                }
                case("Reject1") {
                    send<Unit>(seller, buyer)
                    rec()
                }
                case("Haggle1") {
                    send<Int>(seller, buyer)
                    choice(buyer) {
                        case("Accept2") {
                            send<Unit>(buyer, seller)
                            rec()
                        }
                        case("Reject2") {
                            send<Unit>(buyer, seller)
                            rec()
                        }
                        case("Haggle2") {
                            send<Unit>(buyer, seller)
                            rec()
                        }
                    }
                }
            }
        }

        globalProtocol {
            send<Int>(buyer, seller)
            exec(aux)
        }.dump()


    }
}