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
                branch("Accept1") {
                    send<Unit>(seller, buyer)
                    send<Unit>(buyer, seller)
                }
                branch("Reject1") {
                    send<Unit>(seller, buyer)
                }
                branch("Haggle1") {
                    send<Int>(seller, buyer, "counter", "counter > proposal")
                    choice(buyer) {
                        branch("Accept2") {
                            send<Unit>(buyer, seller)
                            send<Unit>(seller, buyer)
                        }
                        branch("Reject2") {
                            send<Unit>(buyer, seller)
                        }
                        branch("Haggle2") {
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
                                    "Haggle2" to LocalTypeSend(seller, IntClass, LocalTypeRecursion(t), "Haggle2", MsgLabel("proposal2", true), "proposal2 < counter")
                                )
                            ),
                            MsgLabel("counter", true)
                        )
                    )
                )
            ),
            msgLabel = MsgLabel("proposal", true)
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
                                    "Haggle2" to LocalTypeReceive(buyer, IntClass, LocalTypeRecursion(t), MsgLabel("proposal2", true))
                                )
                            ),
                            "Haggle1", MsgLabel("counter", true), "counter > proposal"
                        )
                    )
                )
            ),
            MsgLabel("proposal", true)
        )
        assertEquals(lBuyer, g.project(buyer))
        assertEquals(lSeller, g.project(seller))
    }
}
