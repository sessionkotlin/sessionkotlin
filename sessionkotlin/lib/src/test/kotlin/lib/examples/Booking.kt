package lib.examples

import lib.util.IntClass
import lib.util.StringClass
import lib.util.UnitClass
import org.david.sessionkotlin_lib.dsl.RecursionTag
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Booking {

    @Test
    fun main() {
        val client = Role("Client")
        val agency = Role("Agency")
        val company = Role("Some company")

        lateinit var t: RecursionTag

        val g = globalProtocol {
            t = miu("X")
            choice(client) {
                case("Book") {
                    send<String>(client, agency)
                    send<Int>(agency, client)
                    send<Unit>(agency, company) // dummy message
                    goto(t)
                }
                case("Terminate") {
                    choice(client) {
                        case("Confirm") {
                            send<Unit>(client, agency)
                            send<Unit>(agency, company)
                            send<PaymentInfo>(client, company)
                            send<Unit>(company, client)
                        }
                        case("Cancel") {
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
                        LocalTypeReceive(agency, IntClass, LocalTypeRecursion(t))
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
                                )
                            ),
                            "Cancel" to LocalTypeSend(agency, UnitClass, LEnd)

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
                        LocalTypeSend(client, IntClass, LocalTypeSend(company, UnitClass, LocalTypeRecursion(t)))
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
                                    LEnd
                                )
                            ),
                            "Cancel" to LocalTypeReceive(
                                client,
                                UnitClass,
                                LocalTypeSend(company, UnitClass, LEnd)
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
        assertEquals(g.project(client), lClient)
        assertEquals(g.project(agency), lAgency)
        assertEquals(g.project(company), lCompany)
    }

    class PaymentInfo
}
