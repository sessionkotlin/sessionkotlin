package dsl.examples

import dsl.util.IntClass
import dsl.util.UnitClass
import org.david.sessionkotlin.dsl.RecursionTag
import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.globalProtocolInternal
import org.david.sessionkotlin.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class Adder {

    @Test
    fun main() {
        val c = SKRole("Client")
        val s = SKRole("Server")
        lateinit var t: RecursionTag

        val g = globalProtocolInternal {
            t = miu("X")
            choice(c) {
                case("Continue") {
                    send<Int>(c, s)
                    send<Int>(c, s)
                    send<Int>(s, c)
                    goto(t)
                }
                case("Quit") {
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
                        LocalTypeSend(s, IntClass, LocalTypeReceive(s, IntClass, LocalTypeRecursion(t))),
                        "Continue"
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
                        LocalTypeReceive(c, IntClass, LocalTypeSend(c, IntClass, LocalTypeRecursion(t)))
                    ),
                    "Quit" to LocalTypeReceive(c, UnitClass, LEnd)
                )
            )
        )
        assertEquals(lC, g.project(c))
        assertEquals(lS, g.project(s))
    }
}
