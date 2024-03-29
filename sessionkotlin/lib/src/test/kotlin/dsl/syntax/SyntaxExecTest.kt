package dsl.syntax

import com.github.sessionkotlin.lib.dsl.GlobalProtocol
import com.github.sessionkotlin.lib.dsl.RecursionTag
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.exception.SendingToSelfException
import com.github.sessionkotlin.lib.dsl.globalProtocolInternal
import com.github.sessionkotlin.lib.dsl.types.*
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
                    branch {
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
                branch {
                    send<String>(x, y, "1")
                    send<String>(y, x)
                }
                branch {
                    send<Unit>(x, y, "2")
                }
            }
        }
        val g = globalProtocolInternal {
            subProtocol(b, a)() // reverse roles
        }

        val lB = LocalTypeInternalChoice(
            listOf(
                LocalTypeSend(
                    a,
                    StringClass,
                    MsgLabel("1"),
                    LocalTypeReceive(a, StringClass, LocalTypeEnd)
                ),
                LocalTypeSend(a, UnitClass, MsgLabel("2"), LocalTypeEnd)
            )
        )
        val lA = LocalTypeExternalChoice(
            b,
            listOf(
                LocalTypeReceive(b, StringClass, MsgLabel("1"), LocalTypeSend(b, StringClass, LocalTypeEnd)),
                LocalTypeReceive(b, UnitClass, MsgLabel("2"), LocalTypeEnd)
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
