package dsl.unfinished

import dsl.util.IntClass
import dsl.util.LongClass
import dsl.util.StringClass
import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.exception.UnfinishedRolesException
import org.david.sessionkotlin.dsl.globalProtocolInternal
import org.david.sessionkotlin.dsl.types.LEnd
import org.david.sessionkotlin.dsl.types.LocalTypeReceive
import org.david.sessionkotlin.dsl.types.LocalTypeSend
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnfinishedExecTest {
    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
        val d = SKRole("D")
    }

    @Test
    fun `choice agnostic`() {
        val aux = globalProtocolInternal {
            send<String>(b, c)
            send<Int>(a, b)
            send<Long>(b, c)
        }
        val g = globalProtocolInternal {
            choice(a) {
                branch("1") {
                    exec(aux)
                }
                branch("2") {
                    exec(aux)
                }
                // branches mergeable for 'b', even without being activated for the first send
            }
        }
        val lB = LocalTypeSend(
            c, StringClass,
            LocalTypeReceive(
                a, IntClass,
                LocalTypeSend(c, LongClass, LEnd)
            ),
            "1"
        )
        assertEquals(lB, g.project(b))
    }

    @Test
    fun `unfinished after map`() {
        val subprotocol = globalProtocolInternal {
            send<Int>(a, b)
            send<Int>(a, c)
        }

        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                send<Int>(a, b)
                send<Int>(a, c)
                choice(a) {
                    branch("1") {
                        exec(subprotocol, mapOf(a to b, b to a))
                    }
                    branch("2") {
                        exec(subprotocol)
                    }
                }
            }
        }
    }

    @Test
    fun `unfinished after map 2`() {
        val subprotocol = globalProtocolInternal {
            send<Int>(a, b)
            send<Int>(a, c)
        }

        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                choice(a) {
                    branch("1") {
                        exec(subprotocol)
                    }
                    branch("2") {
                        exec(subprotocol, mapOf(b to c))
                    }
                }
            }
        }
    }
}
