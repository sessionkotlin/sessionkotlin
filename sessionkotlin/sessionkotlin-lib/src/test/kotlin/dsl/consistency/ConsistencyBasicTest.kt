package dsl.consistency

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.InconsistentExternalChoiceException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import dsl.util.BoolClass
import dsl.util.IntClass
import dsl.util.LongClass
import dsl.util.StringClass
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
                    branch {
                        // 'a' activated
                        send<String>(b, a, "Case1")
                        send<String>(a, c)
                    }
                    branch {
                        // 'a' not activated
                        send<Int>(a, c, "Case2")
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
                    branch {
                        send<Int>(b, c, "Case1")
                        // 'a' enabled by 'c'
                        send<Int>(c, a)
                    }
                    branch {
                        send<Int>(b, c, "Case2")
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
                    branch() {
                        send<String>(b, c, "Case1")
                        send<String>(b, c)
                        // 'a' not enabled
                    }
                    branch() {
                        // 'a' enabled by b
                        send<String>(b, a, "Case2")
                        send<String>(a, c, "Case2")
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
                    branch {
                        // 'c' enabled by 'b'
                        send<String>(b, c, "Case1")
                        send<String>(b, a, "Case1")
                    }
                    branch {
                        send<String>(b, a, "Case2")
                        // 'c' enabled by 'a'
                        send<String>(a, c, "Case2")
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
                    branch {
                        send<String>(b, s, "Ok")
                        // 'a' enabled by 's'
                        send<String>(s, a)
                    }
                    branch {
                        send<String>(b, a, "Quit")
                        // 's' not enabled
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
                branch {
                    send<String>(a, b)
                    send(a, b, BoolClass)
                }
                branch {
                    send(a, b, StringClass)
                    send<Boolean>(a, b)
                }
            }
        }
        val lA = LocalTypeSend(b, StringClass, LocalTypeSend(b, BoolClass, LocalTypeEnd))
        val lB = LocalTypeInternalChoice(
            listOf(
                LocalTypeReceive(
                    a,
                    StringClass,
                    LocalTypeReceive(a, BoolClass, LocalTypeEnd)
                ),
                LocalTypeReceive(
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
                branch {
                    send<Long>(a, b, "1")
                    send<Long>(a, c, "1")

                    // ensure this choice does not override 'a' enabling 'b'
                    choice(c) {
                        branch {
                            send<Int>(c, b, "1.1")
                        }
                        branch {
                            send<String>(c, b, "1.2")
                        }
                    }
                }
                branch {
                    send<Int>(a, b, "2")
                    send<Boolean>(a, c, "2")
                }
            }
        }
        val lA = LocalTypeInternalChoice(
            listOf(
                LocalTypeSend(
                    b,
                    LongClass,
                    MsgLabel("1"),
                    LocalTypeSend(
                        c, LongClass, MsgLabel("1"), LEnd
                    )
                ),
                LocalTypeSend(
                    b,
                    IntClass,
                    MsgLabel("1"),
                    LocalTypeSend(
                        c, BoolClass, MsgLabel("1"), LEnd
                    ),
                ),
            )
        )
        val lB = LocalTypeExternalChoice(
            a,
            listOf(
                LocalTypeReceive(
                    a,
                    LongClass,
                    LocalTypeExternalChoice(
                        c,
                        listOf(
                            LocalTypeReceive(c, IntClass, MsgLabel("1.1"), LEnd),
                            LocalTypeReceive(c, StringClass, MsgLabel("1.2"), LEnd)
                        )
                    )
                ),
                LocalTypeReceive(
                    a,
                    IntClass, LEnd
                )
            )
        )
        val lC = LocalTypeExternalChoice(
            a,
            listOf(
                LocalTypeReceive(
                    a,
                    LongClass,
                    MsgLabel("1"),
                    LocalTypeInternalChoice(
                        listOf(
                            LocalTypeSend(b, IntClass, MsgLabel("1.1"), LEnd),
                            LocalTypeSend(b, StringClass, MsgLabel("1.2"), LEnd)
                        )
                    )
                ),
                LocalTypeReceive(
                    a,
                    BoolClass, MsgLabel("2"), LEnd
                )
            )
        )
        assertEquals(g.project(a), lA)
        assertEquals(g.project(b), lB)
        assertEquals(g.project(c), lC)
    }

    @Test
    fun `valid unguarded external choice`() {
        globalProtocolInternal {
            choice(b) {
                branch {
                    choice(b) {
                        branch {
                            send<Int>(b, a, "1.1")
                        }
                        branch {
                            send<Long>(b, a, "1.2")
                        }
                    }
                }
                branch {
                    send<String>(b, a, "2")
                }
            }
        }
    }

    @Test
    fun `valid unguarded external choice 2`() {
        globalProtocolInternal {
            choice(b) {
                branch {
                    send<Int>(b, c, "pre")
                    choice(c) {
                        branch {
                            send<String>(c, b, "b1")
                            send<Int>(b, a, "b1")
                        }
                        branch {
                            send<Long>(c, b, "b12")
                            send<Long>(b, a, "b12")
                        }
                    }
                }
                branch {
                    send<Int>(b, c, "b2")
                    send<String>(b, a, "b2")
                }
            }
        }
    }

    @Test
    fun `invalid unguarded external choice 2`() {
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocolInternal {
                choice(b) {
                    branch {
                        send<Int>(b, c, "pre")
                        choice(c) {
                            branch {
                                send<String>(c, b, "b1")
                                send<Int>(c, a, "b1") // A receives from C
                            }
                            branch {
                                send<Long>(c, b, "b12")
                                send<Long>(c, a, "b12") // A receives from C
                            }
                        }
                    }
                    branch {
                        send<Int>(b, c, "b2")
                        send<String>(b, a, "b2") // A receives from B
                    }
                }
            }
        }
    }
}
