package lib.examples

import lib.util.BoolClass
import lib.util.IntClass
import lib.util.UnitClass
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class BuyerBrokerSupplier {

    @Test
    fun main() {
        val applicant = Role("Applicant")
        val portal = Role("Application Portal")
        val proc = Role("Processing Department")
        val finance = Role("Finance Department")

        val g = globalProtocol {
            send<Application>(applicant, portal)
            send<Application>(portal, proc)
            send<Boolean>(proc, portal)

            choice(portal) {
                case("Approved") {
                    send<Int>(portal, finance)
                    send<Int>(finance, portal)
                    send<Int>(portal, applicant)
                }
                case("Denied") {
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
                                LocalTypeReceive(finance, IntClass, LocalTypeSend(applicant, IntClass, LEnd))
                            ),
                            "Denied" to LocalTypeSend(finance, UnitClass, LocalTypeSend(applicant, UnitClass, LEnd))
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

        assertEquals(g.project(applicant), lApplicant)
        assertEquals(g.project(portal), lPortal)
        assertEquals(g.project(proc), lProc)
        assertEquals(g.project(finance), lFinance)
    }

    data class Application(
        val name: String,
        val dateOfBirth: Date,
        val salary: Int,
    )
}
