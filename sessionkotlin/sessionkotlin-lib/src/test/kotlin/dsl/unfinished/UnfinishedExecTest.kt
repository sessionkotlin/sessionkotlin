package dsl.unfinished

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.UnfinishedRolesException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.LEnd
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeReceive
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeSend
import dsl.util.IntClass
import dsl.util.LongClass
import dsl.util.StringClass
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
