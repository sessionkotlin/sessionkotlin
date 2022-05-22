package dsl.syntax

import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.SendingToSelfException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import dsl.util.IntClass
import dsl.util.StringClass
import dsl.util.UnitClass
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyntaxExecTest {

    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
        val d = SKRole("D")
    }

    @Test
    fun `basic exec`() {
        val x: GlobalProtocol = {
            send<Int>(b, c)
        }

        val g = globalProtocolInternal {
            send<Int>(a, b)
            x()
        }
        val lA = LocalTypeSend(b, IntClass, LEnd)
        val lB = LocalTypeReceive(a, IntClass, LocalTypeSend(c, IntClass, LEnd))
        val lC = LocalTypeReceive(b, IntClass, LEnd)
        assertEquals(g.project(a), lA)
        assertEquals(g.project(b), lB)
        assertEquals(g.project(c), lC)
    }

    @Test
    fun `basic exec new roles`() {
        val x: GlobalProtocol = {
            send<Int>(c, a)
        }

        val g = globalProtocolInternal {
            send<Int>(a, b)
            x()
        }
        val lC = LocalTypeSend(a, IntClass, LEnd)
        assertEquals(g.project(c), lC)
    }

    @Test
    fun `same role sending and receiving 2`() {
        assertFailsWith<SendingToSelfException> {
            fun aux(x: SKRole): GlobalProtocol = {
                send<Int>(b, x)
            }
            globalProtocolInternal {
                send<Int>(a, b)
                aux(b)()
            }
        }
    }

    @Test
    fun `same role sending and receiving 3`() {
        assertFailsWith<SendingToSelfException> {
            fun aux(x: SKRole): GlobalProtocol = {
                choice(x) {
                    branch("1") {
                        send<Int>(x, b)
                    }
                }
            }
            globalProtocolInternal {
                send<Int>(a, b)
                aux(b)()
            }
        }
    }

    @Test
    fun `reversed roles`() {
        fun subProtocol(x: SKRole, y: SKRole): GlobalProtocol = {
            choice(x) {
                branch("1") {
                    send<String>(x, y)
                    send<String>(y, x)
                }
                branch("2") {
                    send<Unit>(x, y)
                }
            }
        }
        val g = globalProtocolInternal {
            subProtocol(b, a)() // reverse roles
        }

        val lB = LocalTypeInternalChoice(
            mapOf(
                "1" to LocalTypeSend(
                    a,
                    StringClass,
                    LocalTypeReceive(a, StringClass, LocalTypeEnd),
                    "1"
                ),
                "2" to LocalTypeSend(a, UnitClass, LocalTypeEnd, "2")
            )
        )
        val lA = LocalTypeExternalChoice(
            b,
            mapOf(
                "1" to LocalTypeReceive(b, StringClass, LocalTypeSend(b, StringClass, LocalTypeEnd)),
                "2" to LocalTypeReceive(b, UnitClass, LocalTypeEnd)
            )
        )
        assertEquals(lA, g.project(a))
        assertEquals(lB, g.project(b))
    }

    @Test
    fun `init test`() {
        lateinit var t: RecursionTag
        val aux: GlobalProtocol = {
            t = mu()
            send<Int>(a, b)
        }
        globalProtocolInternal {
            aux()
            goto(t)
        }
    }

    @Test
    fun `init test 2`() {
        lateinit var t: RecursionTag
        val aux: GlobalProtocol = {
            send<Int>(a, b)
            goto(t)
        }
        globalProtocolInternal {
            t = mu()
            aux()
        }
    }
}
