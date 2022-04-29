package dsl.consistency

import dsl.util.BoolClass
import dsl.util.IntClass
import dsl.util.LongClass
import dsl.util.StringClass
import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.exception.InconsistentExternalChoiceException
import org.david.sessionkotlin.dsl.globalProtocolInternal
import org.david.sessionkotlin.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ConsistencyBasicTest {
    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
    }

    @Test
    fun `role not activated in one branch`() {
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocolInternal {
                choice(b) {
                    branch("Case1") {
                        // 'a' activated
                        send<String>(b, a)
                        send<String>(a, c)
                    }
                    branch("Case2") {
                        // 'a' not activated
                        send<Int>(a, c)
                        send<Int>(c, a)
                    }
                }
            }
        }
    }

    @Test
    fun `role not activated in one branch 2`() {
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocolInternal {
                choice(b) {
                    branch("Case1") {
                        send<Int>(b, c)
                        // 'a' enabled by 'c'
                        send<Int>(c, a)
                    }
                    branch("Case2") {
                        send<Int>(b, c)
                        // 'a' enabled by 'b'
                        send<String>(b, a)
                    }
                }
            }
        }
    }

    @Test
    fun `role not activated in one branch 3`() {
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocolInternal {
                choice(b) {
                    branch("Case1") {
                        send<String>(b, c)
                        send<String>(b, c)
                        // 'a' not enabled
                    }
                    branch("Case2") {
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
            globalProtocolInternal {
                choice(b) {
                    branch("Case1") {
                        // 'c' enabled by 'b'
                        send<String>(b, c)
                    }
                    branch("Case2") {
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
        val s = SKRole("S")

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocolInternal {
                send<String>(a, s)
                send<Int>(s, a)
                send<Int>(s, b)
                send<Int>(a, b)
                choice(b) {
                    branch("Ok") {
                        send<String>(b, s)
                        // 'a' enabled by 's'
                        send<String>(s, a)
                    }
                    branch("Quit") {
                        send<String>(b, a)
                        // 'a' not enabled
                    }
                }
            }
        }
    }

    @Test
    fun `merge inlined and non inlined`() {
        val g = globalProtocolInternal {
            choice(b) {
                // 'a' is not enabled in any branch
                branch("1") {
                    send<String>(a, b)
                    send(a, b, BoolClass)
                }
                branch("2") {
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
        assertEquals(lA, g.project(a))
        assertEquals(lB, g.project(b))
    }

    @Test
    fun `test enabled by`() {
        val g = globalProtocolInternal {
            choice(a) {
                branch("1") {
                    send<Long>(a, b)
                    send<Long>(a, c)

                    // ensure this choice does not override 'a' enabling 'b'
                    choice(c) {
                        branch("1.1") {
                            send<Int>(c, b)
                        }
                        branch("1.2") {
                            send<String>(c, b)
                        }
                    }
                }
                branch("2") {
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
                        c, LongClass, LEnd, "1"
                    ),
                    "1"
                ),
                "2" to LocalTypeSend(
                    b,
                    IntClass,
                    LocalTypeSend(
                        c, BoolClass, LEnd, "2"
                    ),
                    "2"
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
                            "1.1" to LocalTypeSend(b, IntClass, LEnd, "1.1"),
                            "1.2" to LocalTypeSend(b, StringClass, LEnd, "1.2")
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
