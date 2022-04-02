package lib.consistency

import lib.util.IntClass
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.InconsistentExternalChoiceException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConsistencyExecTest {
    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
    }

    @Test
    fun `inconsistent external in exec`() {
        val case1 = globalProtocol {
            send<Int>(b, c)
            send<Int>(c, a)
            // 'a' enabled by 'c'
        }

        val case2 = globalProtocol {
            send<Int>(b, c)
            send<String>(b, a)
            // 'a' enabled by 'b'
        }

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {
                    case("Case 1") {
                        exec(case1)
                    }
                    case("Case 2") {
                        exec(case2)
                    }
                }
            }
        }
    }

    @Test
    fun `consistent external in exec after map`() {
        val case1 = globalProtocol {
            send<Int>(b, c)
            send<Int>(c, a) // 'a' will be replaced by 'b'
        }
        val case2 = globalProtocol {
            send<Int>(b, c)
        }
        val g = globalProtocol {
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
                    LocalTypeReceive(c, IntClass, LEnd)
                ),
                "2" to LocalTypeSend(c, IntClass, LEnd)
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
        assertEquals(g.project(b), lB)
        assertEquals(g.project(c), lC)
    }
}
