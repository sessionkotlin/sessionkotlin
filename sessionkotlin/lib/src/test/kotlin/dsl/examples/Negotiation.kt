package dsl.examples

import dsl.util.IntClass
import dsl.util.UnitClass
import org.david.sessionkotlin.dsl.RecursionTag
import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.globalProtocolInternal
import org.david.sessionkotlin.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Negotiation {

    @Test
    fun main() {
        val buyer = SKRole("Buyer")
        val seller = SKRole("Seller")
        lateinit var t: RecursionTag

        val g = globalProtocolInternal {
            send<Int>(buyer, seller)
            t = miu("X")

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
        }
        val lBuyer = LocalTypeSend(
            seller, IntClass,
            LocalTypeRecursionDefinition(
                t,
                LocalTypeExternalChoice(
                    seller,
                    mapOf(
                        "Accept1" to LocalTypeReceive(seller, UnitClass, LocalTypeSend(seller, UnitClass, LEnd)),
                        "Reject1" to LocalTypeReceive(seller, UnitClass, LEnd),
                        "Haggle1" to LocalTypeReceive(
                            seller, IntClass,
                            LocalTypeInternalChoice(
                                mapOf(
                                    "Accept2" to LocalTypeSend(
                                        seller, UnitClass,
                                        LocalTypeReceive(seller, UnitClass, LEnd),
                                        "Accept2"
                                    ),
                                    "Reject2" to LocalTypeSend(seller, UnitClass, LEnd, "Reject2"),
                                    "Haggle2" to LocalTypeSend(seller, IntClass, LocalTypeRecursion(t), "Haggle2")
                                )
                            )
                        )
                    )
                )
            )
        )
        val lSeller = LocalTypeReceive(
            buyer, IntClass,
            LocalTypeRecursionDefinition(
                t,
                LocalTypeInternalChoice(
                    mapOf(
                        "Accept1" to LocalTypeSend(buyer, UnitClass, LocalTypeReceive(buyer, UnitClass, LEnd), "Accept1"),
                        "Reject1" to LocalTypeSend(buyer, UnitClass, LEnd, "Reject1"),
                        "Haggle1" to LocalTypeSend(
                            buyer, IntClass,
                            LocalTypeExternalChoice(
                                buyer,
                                mapOf(
                                    "Accept2" to LocalTypeReceive(
                                        buyer, UnitClass,
                                        LocalTypeSend(buyer, UnitClass, LEnd)
                                    ),
                                    "Reject2" to LocalTypeReceive(buyer, UnitClass, LEnd),
                                    "Haggle2" to LocalTypeReceive(buyer, IntClass, LocalTypeRecursion(t))
                                )
                            ),
                            "Haggle1"
                        )
                    )
                )
            )
        )
        assertEquals(lBuyer, g.project(buyer))
        assertEquals(lSeller, g.project(seller))
    }
}
