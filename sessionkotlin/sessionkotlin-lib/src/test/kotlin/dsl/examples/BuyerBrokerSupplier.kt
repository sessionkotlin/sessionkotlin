package dsl.examples

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import com.github.d_costa.sessionkotlin.parser.RefinementCondition
import com.github.d_costa.sessionkotlin.parser.symbols.LowerEq
import com.github.d_costa.sessionkotlin.parser.symbols.Name
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
                    send<Int>(portal, applicant, "OK")
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
                    LocalTypeReceive(portal, IntClass, MsgLabel("OK"), LEnd),
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
                                IntClass, MsgLabel("askedAmount", true),
                                LocalTypeReceive(
                                    finance, IntClass, MsgLabel("approvedAmount", true),
                                    LocalTypeSend(applicant, IntClass, MsgLabel("OK"), LEnd)
                                ),
                            ),
                            LocalTypeSend(finance, UnitClass, LocalTypeSend(applicant, UnitClass, LEnd))
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
                    IntClass, MsgLabel("askedAmount", true),
                    LocalTypeSend(
                        portal, IntClass, MsgLabel("approvedAmount", true),
                        RefinementCondition(
                            "approvedAmount <= askedAmount",
                            LowerEq(Name("approvedAmount"), Name("askedAmount"))
                        ),
                        LEnd
                    ),
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
