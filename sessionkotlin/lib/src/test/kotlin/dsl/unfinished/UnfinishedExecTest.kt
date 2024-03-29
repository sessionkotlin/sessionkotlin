package dsl.unfinished

import com.github.sessionkotlin.lib.dsl.GlobalProtocol
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.exception.UnfinishedRolesException
import com.github.sessionkotlin.lib.dsl.globalProtocolInternal
import com.github.sessionkotlin.lib.dsl.types.LEnd
import com.github.sessionkotlin.lib.dsl.types.LocalTypeSend
import dsl.util.LongClass
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
        fun aux(label: String): GlobalProtocol = {
            send<String>(a, c, label)
            send<Long>(b, c)
        }
        val g = globalProtocolInternal {
            choice(a) {
                branch {
                    aux("b1")()
                }
                branch {
                    aux("b2")()
                }
                // branches mergeable for 'b', even without being activated for the first send
            }
        }
        val lB = LocalTypeSend(
            c, LongClass,
            LEnd
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
                    branch {
                        subProtocol(b, a)()
                    }
                    branch {
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
                    branch {
                        subProtocol(b)()
                    }
                    branch {
                        subProtocol(c)()
                    }
                }
            }
        }
    }
}
