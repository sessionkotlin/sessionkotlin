package dsl.syntax

import dsl.util.IntClass
import dsl.util.StringClass
import dsl.util.UnitClass
import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.exception.SendingToSelfException
import org.david.sessionkotlin.dsl.globalProtocolInternal
import org.david.sessionkotlin.dsl.types.*
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
        val x = globalProtocolInternal {
            send<Int>(b, c)
        }

        val g = globalProtocolInternal {
            send<Int>(a, b)
            exec(x)
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
        val x = globalProtocolInternal {
            send<Int>(c, a)
        }

        val g = globalProtocolInternal {
            send<Int>(a, b)
            exec(x)
        }
        val lC = LocalTypeSend(a, IntClass, LEnd)
        assertEquals(g.project(c), lC)
    }

    @Test
    fun `same role sending and receiving 2`() {
        assertFailsWith<SendingToSelfException> {
            val x = globalProtocolInternal {
                send<Int>(b, c)
            }
            globalProtocolInternal {
                send<Int>(a, b)
                exec(x, mapOf(c to b))
            }
        }
    }

    @Test
    fun `same role sending and receiving 3`() {
        assertFailsWith<SendingToSelfException> {
            val x = globalProtocolInternal {
                choice(a) {
                    branch("1") {
                        send<Int>(a, b)
                    }
                }
            }

            globalProtocolInternal {
                send<Int>(a, b)
                exec(x, mapOf(a to b))
            }
        }
    }

    @Test
    fun `roles in map but not in protocol`() {
        val subprotocol = globalProtocolInternal {
            send<Int>(a, c)
        }
        val x = SKRole("X")

        val g = globalProtocolInternal {
            choice(a) {
                branch("1") {
                    exec(subprotocol)
                }
                branch("2") {
                    // 'x' to 'c' is ignored
                    exec(subprotocol, mapOf(x to c))
                }
            }
        }
        assert(!g.roles.contains(x))
    }

    @Test
    fun `reversed roles`() {
        val subprotocol = globalProtocolInternal {
            choice(a) {
                branch("1") {
                    send<String>(a, b)
                    send<String>(b, a)
                }
                branch("2") {
                    send<Unit>(a, b)
                }
            }
        }
        val g = globalProtocolInternal {
            exec(subprotocol, mapOf(a to b, b to a)) // reverse roles
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
}
