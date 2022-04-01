package lib.consistency

import lib.util.BoolClass
import lib.util.IntClass
import lib.util.LongClass
import lib.util.StringClass
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.InconsistentExternalChoiceException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConsistencyBasicTest {
    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
    }

    @Test
    fun `role not activated in one case`() {
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        // 'a' activated
                        send<String>(b, a)
                        send<String>(a, c)
                    }
                    case("Case2") {
                        // 'a' not activated
                        send<Int>(a, c)
                        send<Int>(c, a)
                    }
                }
            }
        }
    }

    @Test
    fun `role not activated in one case 2`() {
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        send<Int>(b, c)
                        // 'a' enabled by 'c'
                        send<Int>(c, a)
                    }
                    case("Case2") {
                        send<Int>(b, c)
                        // 'a' enabled by 'b'
                        send<String>(b, a)
                    }
                }
            }
        }
    }

    @Test
    fun `role not activated in one case 3`() {
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        send<String>(b, c)
                        send<String>(b, c)
                        // 'a' not enabled
                    }
                    case("Case2") {
                        // 'a' enabled by b
                        send<String>(b, a)
                        send<String>(a, c)
                    }
                }
            }
        }
    }

    @Test
    fun `enabled by different roles`() {

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        // 'c' enabled by 'b'
                        send<String>(b, c)
                    }
                    case("Case2") {
                        send<String>(b, a)
                        // 'c' enabled by 'a'
                        send<String>(a, c)
                    }
                }
            }
        }
    }

    @Test
    fun `two buyers inconsistent`() {
        val s = Role("S")

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                send<String>(a, s)
                send<Int>(s, a)
                send<Int>(s, b)
                send<Int>(a, b)
                choice(b) {
                    case("Ok") {
                        send<String>(b, s)
                        // 'a' enabled by 's'
                        send<String>(s, a)
                    }
                    case("Quit") {
                        send<String>(b, a)
                        // 'a' not enabled
                    }
                }
            }
        }
    }

    @Test
    fun `merge inlined and non inlined`() {
        val g = globalProtocol {
            choice(b) {
                // 'a' is not enabled in any branch
                case("1") {
                    send<String>(a, b)
                    send(a, b, BoolClass)
                }
                case("2") {
                    send(a, b, StringClass)
                    send<Boolean>(a, b)
                }
            }
        }
        val lA = LocalTypeSend(b, StringClass, LocalTypeSend(b, BoolClass, LocalTypeEnd))
        val lB = LocalTypeInternalChoice(
            mapOf(
                "1" to LocalTypeReceive(
                    a,
                    StringClass,
                    LocalTypeReceive(a, BoolClass, LocalTypeEnd)
                ),
                "2" to LocalTypeReceive(
                    a,
                    StringClass,
                    LocalTypeReceive(a, BoolClass, LocalTypeEnd)
                )
            )
        )
        assertEquals(g.project(a), lA)
        assertEquals(g.project(b), lB)
    }

    @Test
    fun `test enabled by`() {
        val g = globalProtocol {
            choice(a) {
                case("1") {
                    send<Long>(a, b)
                    send<Long>(a, c)

                    // ensure this choice does not override 'a' enabling 'b'
                    choice(c) {
                        case("1.1") {
                            send<Int>(c, b)
                        }
                        case("1.2") {
                            send<String>(c, b)
                        }
                    }
                }
                case("2") {
                    send<Int>(a, b)
                    send<Boolean>(a, c)
                }
            }
        }
        val lA = LocalTypeInternalChoice(
            mapOf(
                "1" to LocalTypeSend(
                    b,
                    LongClass,
                    LocalTypeSend(
                        c, LongClass, LEnd
                    )
                ),
                "2" to LocalTypeSend(
                    b,
                    IntClass,
                    LocalTypeSend(
                        c, BoolClass, LEnd
                    )
                ),
            )
        )
        val lB = LocalTypeExternalChoice(
            a,
            mapOf(
                "1" to LocalTypeReceive(
                    a,
                    LongClass,
                    LocalTypeExternalChoice(
                        c,
                        mapOf(
                            "1.1" to LocalTypeReceive(c, IntClass, LEnd),
                            "1.2" to LocalTypeReceive(c, StringClass, LEnd)
                        )
                    )
                ),
                "2" to LocalTypeReceive(
                    a,
                    IntClass, LEnd
                )
            )
        )
        val lC = LocalTypeExternalChoice(
            a,
            mapOf(
                "1" to LocalTypeReceive(
                    a,
                    LongClass,
                    LocalTypeInternalChoice(
                        mapOf(
                            "1.1" to LocalTypeSend(b, IntClass, LEnd),
                            "1.2" to LocalTypeSend(b, StringClass, LEnd)
                        )
                    )
                ),
                "2" to LocalTypeReceive(
                    a,
                    BoolClass, LEnd
                )
            )
        )
        assertEquals(g.project(a), lA)
        assertEquals(g.project(b), lB)
        assertEquals(g.project(c), lC)
    }
}
