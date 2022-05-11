package dsl.consistency

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.InconsistentExternalChoiceException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import dsl.util.IntClass
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConsistencyExecTest {
    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
    }

    @Test
    fun `inconsistent external in exec`() {
        val case1 = globalProtocolInternal {
            send<Int>(b, c)
            send<Int>(c, a)
            // 'a' enabled by 'c'
        }

        val case2 = globalProtocolInternal {
            send<Int>(b, c)
            send<String>(b, a)
            // 'a' enabled by 'b'
        }

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocolInternal {
                choice(b) {
                    branch("Case1") {
                        exec(case1)
                    }
                    branch("Case2") {
                        exec(case2)
                    }
                }
            }
        }
    }

    @Test
    fun `consistent external in exec after map`() {
        val case1 = globalProtocolInternal {
            send<Int>(b, c)
            send<Int>(c, a) // 'a' will be replaced by 'b'
        }
        val case2 = globalProtocolInternal {
            send<Int>(b, c)
        }
        val g = globalProtocolInternal {
            choice(b) {
                branch("1") {
                    exec(case1, mapOf(a to b))
                }
                branch("2") {
                    exec(case2)
                }
            }
        }
        val lB = LocalTypeInternalChoice(
            mapOf(
                "1" to LocalTypeSend(
                    c, IntClass,
                    LocalTypeReceive(c, IntClass, LEnd), "1"
                ),
                "2" to LocalTypeSend(c, IntClass, LEnd, "2")
            )
        )
        val lC = LocalTypeExternalChoice(
            b,
            mapOf(
                "1" to LocalTypeReceive(
                    b, IntClass,
                    LocalTypeSend(b, IntClass, LEnd)
                ),
                "2" to LocalTypeReceive(b, IntClass, LEnd)
            )
        )
        assertEquals(lB, g.project(b))
        assertEquals(lC, g.project(c))
    }
}
