package dsl.examples

import com.github.sessionkotlin.lib.dsl.RecursionTag
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.globalProtocolInternal
import com.github.sessionkotlin.lib.dsl.types.*
import dsl.util.IntClass
import dsl.util.StringClass
import dsl.util.UnitClass
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Booking {

    @Test
    fun main() {
        val client = SKRole("Client")
        val agency = SKRole("Agency")
        val company = SKRole("SomeCompany")

        lateinit var t: RecursionTag

        val g = globalProtocolInternal {
            t = mu()
            choice(client) {
                branch {
                    send<String>(client, agency, "request")
                    send<Int>(agency, client, "amount")
                    send<Unit>(agency, company) // dummy message
                    goto(t)
                }
                branch {
                    choice(client) {
                        branch {
                            send<Unit>(client, agency, "confirm")
                            send<Unit>(agency, company, "confirm")
                            send<PaymentInfo>(client, company)
                            send<Unit>(company, client)
                        }
                        branch {
                            send<Unit>(client, agency, "cancel")
                            send<Unit>(agency, company, "cancel")
                        }
                    }
                }
            }
        }

        val lClient = LocalTypeRecursionDefinition(
            t,
            LocalTypeInternalChoice(
                listOf(
                    LocalTypeSend(
                        agency,
                        StringClass, MsgLabel("request"),
                        LocalTypeReceive(agency, IntClass, MsgLabel("amount"), LocalTypeRecursion(t)),
                    ),
                    LocalTypeInternalChoice(
                        listOf(
                            LocalTypeSend(
                                agency,
                                UnitClass, MsgLabel("confirm"),
                                LocalTypeSend(
                                    company,
                                    PaymentInfo::class.java,
                                    LocalTypeReceive(company, UnitClass, LEnd)
                                ),
                            ),
                            LocalTypeSend(agency, UnitClass, MsgLabel("cancel"), LEnd)
                        )
                    )

                )
            )
        )
        val lAgency = LocalTypeRecursionDefinition(
            t,
            LocalTypeExternalChoice(
                client,
                listOf(
                    LocalTypeReceive(
                        client,
                        StringClass, MsgLabel("request"),
                        LocalTypeSend(
                            client,
                            IntClass,
                            MsgLabel("amount"),
                            LocalTypeSend(company, UnitClass, LocalTypeRecursion(t))
                        )
                    ),
                    LocalTypeExternalChoice(
                        client,
                        listOf(
                            LocalTypeReceive(
                                client,
                                UnitClass, MsgLabel("confirm"),
                                LocalTypeSend(
                                    company,
                                    UnitClass, MsgLabel("confirm"),
                                    LEnd,
                                )
                            ),
                            LocalTypeReceive(
                                client,
                                UnitClass, MsgLabel("cancel"),
                                LocalTypeSend(company, UnitClass, MsgLabel("cancel"), LEnd)
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
                listOf(
                    LocalTypeReceive(
                        agency,
                        UnitClass,
                        LocalTypeRecursion(t)
                    ),
                    LocalTypeExternalChoice(
                        agency,
                        listOf(
                            LocalTypeReceive(
                                agency,
                                UnitClass, MsgLabel("confirm"),
                                LocalTypeReceive(
                                    client, PaymentInfo::class.java,
                                    LocalTypeSend(
                                        client,
                                        UnitClass,
                                        LEnd
                                    )
                                )
                            ),
                            LocalTypeReceive(agency, UnitClass, MsgLabel("cancel"), LEnd)
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
