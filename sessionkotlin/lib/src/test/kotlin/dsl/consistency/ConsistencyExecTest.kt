package dsl.consistency

import dsl.util.IntClass
import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.exception.InconsistentExternalChoiceException
import org.david.sessionkotlin.dsl.globalProtocolInternal
import org.david.sessionkotlin.dsl.types.*
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
                    case("Case1") {
                        exec(case1)
                    }
                    case("Case2") {
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
                case("1") {
                    exec(case1, mapOf(a to b))
                }
                case("2") {
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
