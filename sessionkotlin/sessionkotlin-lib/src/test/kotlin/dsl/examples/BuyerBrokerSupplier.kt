package dsl.examples

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import dsl.util.BoolClass
import dsl.util.IntClass
import dsl.util.UnitClass
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class BuyerBrokerSupplier {

    @Test
    fun main() {
        val applicant = SKRole("Applicant")
        val portal = SKRole("ApplicationPortal")
        val proc = SKRole("ProcessingDepartment")
        val finance = SKRole("FinanceDepartment")

        val g = globalProtocolInternal {
            send<Application>(applicant, portal)
            send<Application>(portal, proc)
            send<Boolean>(proc, portal)

            choice(portal) {
                branch("Approved") {
                    send<Int>(portal, finance, "askedAmount")
                    send<Int>(finance, portal, "approvedAmount", "approvedAmount <= askedAmount")
                    send<Int>(portal, applicant, "x", "x == approvedAmount")
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
                    "Approved" to LocalTypeReceive(portal, IntClass, LEnd, MsgLabel("x", true)),
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
                                LocalTypeReceive(finance, IntClass, LocalTypeSend(applicant, IntClass, LEnd, "Approved", MsgLabel("x", true), "x == approvedAmount"), MsgLabel("approvedAmount", true)),
                                "Approved", MsgLabel("askedAmount", true)
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
                    LocalTypeSend(portal, IntClass, LEnd, msgLabel = MsgLabel("approvedAmount", true), condition = "approvedAmount <= askedAmount"),
                    MsgLabel("askedAmount", true)
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
