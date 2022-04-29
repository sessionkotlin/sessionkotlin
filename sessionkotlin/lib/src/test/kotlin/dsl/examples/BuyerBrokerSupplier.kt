package dsl.examples

import dsl.util.BoolClass
import dsl.util.IntClass
import dsl.util.UnitClass
import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.globalProtocolInternal
import org.david.sessionkotlin.dsl.types.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class BuyerBrokerSupplier {

    @Test
    fun main() {
        val applicant = SKRole("Applicant")
        val portal = SKRole("Application Portal")
        val proc = SKRole("Processing Department")
        val finance = SKRole("Finance Department")

        val g = globalProtocolInternal {
            send<Application>(applicant, portal)
            send<Application>(portal, proc)
            send<Boolean>(proc, portal)

            choice(portal) {
                branch("Approved") {
                    send<Int>(portal, finance)
                    send<Int>(finance, portal)
                    send<Int>(portal, applicant)
                }
                branch("Denied") {
                    send<Unit>(portal, finance)
                    send<Unit>(portal, applicant)
                }
            }
        }

        val lApplicant = LocalTypeSend(
            portal, Application::class.java,
            LocalTypeExternalChoice(
                portal,
                mapOf(
                    "Approved" to LocalTypeReceive(portal, IntClass, LEnd),
                    "Denied" to LocalTypeReceive(portal, UnitClass, LEnd)
                )
            )
        )
        val lPortal = LocalTypeReceive(
            applicant, Application::class.java,
            LocalTypeSend(
                proc, Application::class.java,
                LocalTypeReceive(
                    proc, BoolClass,
                    LocalTypeInternalChoice(
                        mapOf(
                            "Approved" to LocalTypeSend(
                                finance,
                                IntClass,
                                LocalTypeReceive(finance, IntClass, LocalTypeSend(applicant, IntClass, LEnd, "Approved")),
                                "Approved"
                            ),
                            "Denied" to LocalTypeSend(finance, UnitClass, LocalTypeSend(applicant, UnitClass, LEnd, "Denied"), "Denied")
                        )
                    )
                )
            )
        )
        val lProc = LocalTypeReceive(portal, Application::class.java, LocalTypeSend(portal, BoolClass, LEnd))
        val lFinance = LocalTypeExternalChoice(
            portal,
            mapOf(
                "Approved" to LocalTypeReceive(
                    portal,
                    IntClass,
                    LocalTypeSend(portal, IntClass, LEnd)
                ),
                "Denied" to LocalTypeReceive(portal, UnitClass, LEnd)
            )
        )

        assertEquals(lApplicant, g.project(applicant))
        assertEquals(lPortal, g.project(portal))
        assertEquals(lProc, g.project(proc))
        assertEquals(lFinance, g.project(finance))
    }

    data class Application(
        val name: String,
        val dateOfBirth: Date,
        val salary: Int,
    )
}
