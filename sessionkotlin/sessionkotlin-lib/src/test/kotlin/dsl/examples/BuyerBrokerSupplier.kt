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
                branch {
                    send<Int>(portal, finance, "askedAmount")
                    send<Int>(finance, portal, "approvedAmount", "approvedAmount <= askedAmount")
                    send<Int>(portal, applicant, "x", "x == approvedAmount")
                }
                branch {
                    send<Unit>(portal, finance)
                    send<Unit>(portal, applicant)
                }
            }
        }

        val lApplicant = LocalTypeSend(
            portal, Application::class.java,
            LocalTypeExternalChoice(
                portal,
                listOf(
                    LocalTypeReceive(portal, IntClass, LEnd),
                    LocalTypeReceive(portal, UnitClass, LEnd)
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
                        listOf(
                            LocalTypeSend(
                                finance,
                                IntClass,
                                LocalTypeReceive(finance, IntClass, LocalTypeSend(applicant, IntClass, LEnd)),
                            ),
                            LocalTypeSend(finance, UnitClass,  MsgLabel("Denied"), LocalTypeSend(applicant, UnitClass, MsgLabel("Denied"), LEnd))
                        )
                    )
                )
            )
        )
        val lProc = LocalTypeReceive(portal, Application::class.java, LocalTypeSend(portal, BoolClass, LEnd))
        val lFinance = LocalTypeExternalChoice(
            portal,
            listOf(
                LocalTypeReceive(
                    portal,
                    IntClass,
                    LocalTypeSend(portal, IntClass, LEnd),
                ),
                LocalTypeReceive(portal, UnitClass, LEnd)
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
