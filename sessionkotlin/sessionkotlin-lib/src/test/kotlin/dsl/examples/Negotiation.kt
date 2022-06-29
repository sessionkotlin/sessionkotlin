package dsl.examples

import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import dsl.util.IntClass
import dsl.util.UnitClass
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Negotiation {

    @Test
    fun main() {
        val buyer = SKRole("Buyer")
        val seller = SKRole("Seller")
        lateinit var t: RecursionTag

        val g = globalProtocolInternal {
            send<Int>(buyer, seller, "proposal")
            t = mu()

            choice(seller) {
                branch {
                    send<Unit>(seller, buyer, "Accept1")
                    send<Unit>(buyer, seller, "Accept1")
                }
                branch {
                    send<Unit>(seller, buyer, "Reject1")
                }
                branch {
                    send<Int>(seller, buyer, "counter", "counter > proposal")
                    choice(buyer) {
                        branch {
                            send<Unit>(buyer, seller, "Accept2")
                            send<Unit>(seller, buyer, "Accept2")
                        }
                        branch {
                            send<Unit>(buyer, seller, "Reject2")
                        }
                        branch {
                            send<Int>(buyer, seller, "proposal2", "proposal2 < counter")
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
                    listOf(
                        LocalTypeReceive(seller, UnitClass, LocalTypeSend(seller, UnitClass, LEnd)),
                        LocalTypeReceive(seller, UnitClass, LEnd),
                        LocalTypeReceive(
                            seller, IntClass,
                            LocalTypeInternalChoice(
                                listOf(
                                    LocalTypeSend(
                                        seller, UnitClass,
                                        LocalTypeReceive(seller, UnitClass, LEnd)
                                    ),
                                    LocalTypeSend(seller, UnitClass, LEnd),
                                    LocalTypeSend(seller, IntClass, LocalTypeRecursion(t)))
                                )
                            ),
                        )
                    )
                )
        )
        val lSeller = LocalTypeReceive(
            buyer, IntClass,
            LocalTypeRecursionDefinition(
                t,
                LocalTypeInternalChoice(
                    listOf(
                        LocalTypeSend(buyer, UnitClass, LocalTypeReceive(buyer, UnitClass, LEnd)),
                        LocalTypeSend(buyer, UnitClass, LEnd),
                        LocalTypeSend(
                            buyer, IntClass,
                            LocalTypeExternalChoice(
                                buyer,
                                listOf(
                                    LocalTypeReceive(
                                        buyer, UnitClass,
                                        LocalTypeSend(buyer, UnitClass, LEnd)
                                    ),
                                    LocalTypeReceive(buyer, UnitClass, LEnd),
                                    LocalTypeReceive(buyer, IntClass, LocalTypeRecursion(t))
                                )
                            )
                        )
                    )
                )
            )
        )
        assertEquals(lBuyer, g.project(buyer))
        assertEquals(lSeller, g.project(seller))
    }
}
