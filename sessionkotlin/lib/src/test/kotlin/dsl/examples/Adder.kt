package dsl.examples

import com.github.sessionkotlin.lib.dsl.RecursionTag
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.globalProtocolInternal
import com.github.sessionkotlin.lib.dsl.types.*
import com.github.sessionkotlin.parser.RefinementCondition
import com.github.sessionkotlin.parser.symbols.Eq
import com.github.sessionkotlin.parser.symbols.Name
import com.github.sessionkotlin.parser.symbols.Plus
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
                branch {
                    send<Int>(c, s, "v1")
                    send<Int>(c, s, "v2")
                    send<Int>(s, c, "sum", "sum == v1 + v2")
                    goto(t)
                }
                branch {
                    send<Unit>(c, s)
                }
            }
        }
        val lC = LocalTypeRecursionDefinition(
            t,
            LocalTypeInternalChoice(
                listOf(
                    LocalTypeSend(
                        s, IntClass, MsgLabel("v1", true),
                        LocalTypeSend(
                            s, IntClass, MsgLabel("v2", true),
                            LocalTypeReceive(s, IntClass, MsgLabel("sum", true), LocalTypeRecursion(t))
                        ),
                    ),
                    LocalTypeSend(s, UnitClass, LEnd)
                )
            )
        )
        val lS = LocalTypeRecursionDefinition(
            t,
            LocalTypeExternalChoice(
                c,
                listOf(
                    LocalTypeReceive(
                        c, IntClass, MsgLabel("v1", true),
                        LocalTypeReceive(
                            c, IntClass, MsgLabel("v2", true),
                            LocalTypeSend(
                                c, IntClass, MsgLabel("sum", true),
                                RefinementCondition(
                                    "sum == v1 + v2",
                                    Eq(Name("sum"), Plus(Name("v1"), Name("v2")))
                                ),
                                LocalTypeRecursion(t)
                            )
                        ),
                    ),
                    LocalTypeReceive(c, UnitClass, LEnd)
                )
            )
        )
        assertEquals(lC, g.project(c))
        assertEquals(lS, g.project(s))
    }
}
