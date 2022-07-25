package dsl.examples

import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import com.github.d_costa.sessionkotlin.parser.RefinementCondition
import com.github.d_costa.sessionkotlin.parser.symbols.Greater
import com.github.d_costa.sessionkotlin.parser.symbols.Lower
import com.github.d_costa.sessionkotlin.parser.symbols.Name
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
                    send<Unit>(buyer, seller)
                }
                branch {
                    send<Unit>(seller, buyer, "Reject1")
                }
                branch {
                    send<Int>(seller, buyer, "counter", "counter > proposal")
                    choice(buyer) {
                        branch {
                            send<Unit>(buyer, seller, "Accept2")
                            send<Unit>(seller, buyer,)
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
            seller, IntClass, MsgLabel("proposal", true),
            LocalTypeRecursionDefinition(
                t,
                LocalTypeExternalChoice(
                    seller,
                    listOf(
                        LocalTypeReceive(seller, UnitClass, MsgLabel("Accept1"), LocalTypeSend(seller, UnitClass, LEnd)),
                        LocalTypeReceive(seller, UnitClass, MsgLabel("Reject1"), LEnd),
                        LocalTypeReceive(
                            seller, IntClass, MsgLabel("counter", true),
                            LocalTypeInternalChoice(
                                listOf(
                                    LocalTypeSend(
                                        seller, UnitClass, MsgLabel("Accept2"),
                                        LocalTypeReceive(seller, UnitClass, LEnd)
                                    ),
                                    LocalTypeSend(seller, UnitClass, MsgLabel("Reject2"), LEnd),
                                    LocalTypeSend(
                                        seller, IntClass, MsgLabel("proposal2", true),
                                        RefinementCondition(
                                            "proposal2 < counter",
                                            Lower(Name("proposal2"), Name("counter"))
                                        ),
                                        LocalTypeRecursion(t)
                                    )
                                )
                            )
                        ),
                    )
                )
            )
        )
        val lSeller = LocalTypeReceive(
            buyer, IntClass, MsgLabel("proposal", true),
            LocalTypeRecursionDefinition(
                t,
                LocalTypeInternalChoice(
                    listOf(
                        LocalTypeSend(buyer, UnitClass, MsgLabel("Accept1"), LocalTypeReceive(buyer, UnitClass, LEnd)),
                        LocalTypeSend(buyer, UnitClass, MsgLabel("Reject1"), LEnd),
                        LocalTypeSend(
                            buyer, IntClass, MsgLabel("counter", true),
                            RefinementCondition(
                                "counter > proposal",
                                Greater(Name("counter"), Name("proposal"))
                            ),
                            LocalTypeExternalChoice(
                                buyer,
                                listOf(
                                    LocalTypeReceive(
                                        buyer, UnitClass, MsgLabel("Accept2"),
                                        LocalTypeSend(buyer, UnitClass, LEnd)
                                    ),
                                    LocalTypeReceive(buyer, UnitClass, MsgLabel("Reject2"), LEnd),
                                    LocalTypeReceive(buyer, IntClass, MsgLabel("proposal2", true), LocalTypeRecursion(t))
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
