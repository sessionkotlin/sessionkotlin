package dsl.examples

import dsl.util.IntClass
import dsl.util.StringClass
import dsl.util.UnitClass
import org.david.sessionkotlin.dsl.RecursionTag
import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.globalProtocolInternal
import org.david.sessionkotlin.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Booking {

    @Test
    fun main() {
        val client = SKRole("Client")
        val agency = SKRole("Agency")
        val company = SKRole("Some company")

        lateinit var t: RecursionTag

        val g = globalProtocolInternal {
            t = miu("X")
            choice(client) {
                branch("Book") {
                    send<String>(client, agency)
                    send<Int>(agency, client)
                    send<Unit>(agency, company) // dummy message
                    goto(t)
                }
                branch("Terminate") {
                    choice(client) {
                        branch("Confirm") {
                            send<Unit>(client, agency)
                            send<Unit>(agency, company)
                            send<PaymentInfo>(client, company)
                            send<Unit>(company, client)
                        }
                        branch("Cancel") {
                            send<Unit>(client, agency)
                            send<Unit>(agency, company)
                        }
                    }
                }
            }
        }

        val lClient = LocalTypeRecursionDefinition(
            t,
            LocalTypeInternalChoice(
                mapOf(
                    "Book" to LocalTypeSend(
                        agency,
                        StringClass,
                        LocalTypeReceive(agency, IntClass, LocalTypeRecursion(t)),
                        "Book"
                    ),
                    "Terminate" to LocalTypeInternalChoice(
                        mapOf(
                            "Confirm" to LocalTypeSend(
                                agency,
                                UnitClass,
                                LocalTypeSend(
                                    company,
                                    PaymentInfo::class.java,
                                    LocalTypeReceive(company, UnitClass, LEnd)
                                ),
                                "Confirm"
                            ),
                            "Cancel" to LocalTypeSend(agency, UnitClass, LEnd, "Cancel")

                        )
                    )

                )
            )
        )
        val lAgency = LocalTypeRecursionDefinition(
            t,
            LocalTypeExternalChoice(
                client,
                mapOf(
                    "Book" to LocalTypeReceive(
                        client,
                        StringClass,
                        LocalTypeSend(client, IntClass, LocalTypeSend(company, UnitClass, LocalTypeRecursion(t), "Book"))
                    ),
                    "Terminate" to LocalTypeExternalChoice(
                        client,
                        mapOf(
                            "Confirm" to LocalTypeReceive(
                                client,
                                UnitClass,
                                LocalTypeSend(
                                    company,
                                    UnitClass,
                                    LEnd,
                                    "Confirm"
                                )
                            ),
                            "Cancel" to LocalTypeReceive(
                                client,
                                UnitClass,
                                LocalTypeSend(company, UnitClass, LEnd, "Cancel")
                            )

                        )
                    )

                )
            )
        )
        val lCompany = LocalTypeRecursionDefinition(
            t,
            LocalTypeExternalChoice(
                agency,
                mapOf(
                    "Book" to LocalTypeReceive(
                        agency,
                        UnitClass,
                        LocalTypeRecursion(t)
                    ),
                    "Terminate" to LocalTypeExternalChoice(
                        agency,
                        mapOf(
                            "Confirm" to LocalTypeReceive(
                                agency,
                                UnitClass,
                                LocalTypeReceive(
                                    client, PaymentInfo::class.java,
                                    LocalTypeSend(
                                        client,
                                        UnitClass,
                                        LEnd
                                    )
                                )
                            ),
                            "Cancel" to LocalTypeReceive(agency, UnitClass, LEnd)
                        )
                    )
                )
            )
        )
        assertEquals(lClient, g.project(client))
        assertEquals(lAgency, g.project(agency))
        assertEquals(lCompany, g.project(company))
    }

    class PaymentInfo
}
