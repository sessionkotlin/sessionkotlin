package dsl.examples

import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import dsl.util.IntClass
import dsl.util.UnitClass
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Adder {

    @Test
    fun main() {
        val c = SKRole("Client")
        val s = SKRole("Server")
        lateinit var t: RecursionTag

        val g = globalProtocolInternal {
            t = mu()
            choice(c) {
                branch("Continue") {
                    send<Int>(c, s, "v1")
                    send<Int>(c, s, "v2")
                    send<Int>(s, c, "sum", "sum == v1 + v2")
                    goto(t)
                }
                branch("Quit") {
                    send<Unit>(c, s)
                }
            }
        }
        val lC = LocalTypeRecursionDefinition(
            t,
            LocalTypeInternalChoice(
                mapOf(
                    "Continue" to LocalTypeSend(
                        s, IntClass,
                        LocalTypeSend(s, IntClass, LocalTypeReceive(s, IntClass, LocalTypeRecursion(t), msgLabel = MsgLabel("sum", true)), msgLabel = MsgLabel("v2", true)),
                        "Continue", MsgLabel("v1", true)
                    ),
                    "Quit" to LocalTypeSend(s, UnitClass, LEnd, "Quit")
                )
            )
        )
        val lS = LocalTypeRecursionDefinition(
            t,
            LocalTypeExternalChoice(
                c,
                mapOf(
                    "Continue" to LocalTypeReceive(
                        c, IntClass,
                        LocalTypeReceive(c, IntClass, LocalTypeSend(c, IntClass, LocalTypeRecursion(t), msgLabel = MsgLabel("sum", true), condition = "sum == v1 + v2"), MsgLabel("v2", true)),
                        MsgLabel("v1", true)
                    ),
                    "Quit" to LocalTypeReceive(c, UnitClass, LEnd)
                )
            )
        )
        assertEquals(lC, g.project(c))
        assertEquals(lS, g.project(s))
    }
}
