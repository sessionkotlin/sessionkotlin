package dsl.unfinished

import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
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
        val aux: GlobalProtocol = {
            send<String>(b, c)
            send<Int>(a, b)
            send<Long>(b, c)
        }
        val g = globalProtocolInternal {
            choice(a) {
                branch("1") {
                    aux()
                }
                branch("2") {
                    aux()
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
        fun subProtocol(x: SKRole, y: SKRole): GlobalProtocol = {
            send<Int>(x, y)
            send<Int>(x, c)
        }

        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                send<Int>(a, b)
                send<Int>(a, c)
                choice(a) {
                    branch("1") {
                        subProtocol(b, a)()
                    }
                    branch("2") {
                        subProtocol(a, b)()
                    }
                }
            }
        }
    }

    @Test
    fun `unfinished after map 2`() {
        fun subProtocol(x: SKRole): GlobalProtocol = {
            send<Int>(a, x)
            send<Int>(a, c)
        }

        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                choice(a) {
                    branch("1") {
                        subProtocol(b)()
                    }
                    branch("2") {
                        subProtocol(c)()
                    }
                }
            }
        }
    }
}