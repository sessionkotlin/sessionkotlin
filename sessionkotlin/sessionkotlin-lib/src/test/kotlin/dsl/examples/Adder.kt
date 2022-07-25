package dsl.examples

import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import com.github.d_costa.sessionkotlin.parser.RefinementCondition
import com.github.d_costa.sessionkotlin.parser.symbols.Eq
import com.github.d_costa.sessionkotlin.parser.symbols.Name
import com.github.d_costa.sessionkotlin.parser.symbols.Plus
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
