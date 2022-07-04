package dsl.syntax

import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.*
import com.github.d_costa.sessionkotlin.dsl.globalProtocol
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import dsl.util.DoubleClass
import dsl.util.IntClass
import dsl.util.StringClass
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyntaxBasicTest {

    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
        val d = SKRole("D")
    }

    @Test
    fun `two party`() {
        val g = globalProtocolInternal {
            send<Int>(a, b)
        }
        val lA = LocalTypeSend(b, IntClass, LEnd)
        val lB = LocalTypeReceive(a, IntClass, LEnd)
        assertEquals(g.project(a), lA)
        assertEquals(g.project(b), lB)
    }

    @Test
    fun `four party`() {
        globalProtocolInternal {
            send<String>(a, b)
            send<Double>(a, d)
            send<Int>(d, c)
            send<Long>(c, b)
        }
    }

    @Test
    fun `four party non inlined`() {
        globalProtocolInternal {
            send(a, b, StringClass)
            send(a, d, DoubleClass)
            send(d, c, Int::class.java)
            send(c, b, Long::class.java)
        }
    }

    @Test
    fun `same role sending and receiving`() {
        assertFailsWith<SendingToSelfException> {
            globalProtocolInternal {
                send<Int>(a, a)
            }
        }
    }

    @Test
    fun `three party`() {
        globalProtocolInternal {
            send<Int>(a, b)
            send<Int>(c, b)
            send<Int>(b, a)
        }
    }

    @Test
    fun `simple choice`() {
        globalProtocolInternal {
            choice(b) {
                branch {
                    send<String>(b, a, "b1")
                    send<String>(b, a)
                }
                branch {
                    choice(b) {
                        branch {
                            send<Long>(b, a, "b2")
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `two buyers`() {
        val s = SKRole("S")

        globalProtocolInternal {
            send<String>(a, s)
            send<Int>(s, a)
            send<Int>(s, b)
            send<Int>(a, b)
            choice(b) {
                branch {
                    send<String>(b, s, "1")
                    send<String>(s, b)
                }
                branch {
                    send<String>(b, s, "2")
                }
            }
        }
    }

    @Test
    fun `send after choice`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocolInternal {
                choice(a) {
                    branch {
                        send<Unit>(a, b)
                    }
                    branch {
                        send<Unit>(a, c)
                    }
                }
                // choice is a terminal operation
                send<Unit>(a, b)
            }
        }
    }

    @Test
    fun `dupe case labels`() {
        assertFailsWith<NonDeterministicStatesException> {
            globalProtocolInternal {
                choice(b) {
                    branch {
                        send<String>(b, a)
                    }
                    branch {
                        send<Int>(b, a)
                        send<Long>(a, b)
                    }
                }
            }
        }
    }

    @Test
    fun `dupe branch labels nested`() {
        globalProtocolInternal {
            choice(b) {
                branch {
                    send<String>(b, a, "b1")
                }
                branch {
                    send<Int>(b, a, "b2")
                    send<Long>(a, b, "HERE")
                    choice(a) {
                        branch {
                            send<Int>(a, b, "HERE")
                        }
                        branch {
                            send<Int>(a, b)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `commutative sends in choice branches`() {
        globalProtocolInternal {
            choice(b) {
                branch {
                    send<Int>(b, c, "1")
                    send<String>(b, a, "1")
                }
                branch {
                    send<String>(b, a, "2")
                    send<Int>(b, c, "2")
                }
            }
        }
    }

    @Test
    fun `different actual choice subject`() {
        val g = globalProtocolInternal {
            choice(b) {
                branch {
                    send<String>(b, c, "b1")
                    send<String>(c, a, "b1")
                }
                branch {
                    send<Int>(b, c, "b2")
                    send<Int>(c, a, "b2")
                }
            }
        }
        val lA = LocalTypeExternalChoice(
            c,
            listOf(
                LocalTypeReceive(c, StringClass, MsgLabel("b1"), LEnd),
                LocalTypeReceive(c, IntClass, MsgLabel("b2"), LEnd)
            )
        )
        assertEquals(g.project(a), lA)
    }

    @Test
    fun `different actual choice subject 2`() {
        val g = globalProtocolInternal {
            choice(b) {
                branch {
                    send<Int>(b, c, "1")
                    send<Int>(c, a, "1")
                }
                branch {
                    send<Int>(b, c, "2")
                    send<Int>(c, a, "2")
                }
            }
        }
        val lA = LocalTypeExternalChoice(
            c,
            listOf(
                LocalTypeReceive(c, IntClass, MsgLabel("1"), LEnd),
                LocalTypeReceive(c, IntClass, MsgLabel("2"), LEnd)
            )
        )
        assertEquals(g.project(a), lA)
    }

    @Test
    fun `space in label`() {
        assertFailsWith<BranchLabelWhitespaceException> {
            globalProtocolInternal {
                send<Int>(a, b, " Case1")
            }
        }
    }

    @Test
    fun `multiple spaces in label`() {
        assertFailsWith<BranchLabelWhitespaceException> {
            globalProtocolInternal {
                send<Int>(a, b, "C ase 1")
            }
        }
    }

    @Test
    fun `tab in label`() {
        assertFailsWith<BranchLabelWhitespaceException> {
            globalProtocolInternal {
                send<Unit>(a, b, "before\tafter")
            }
        }
    }

    @Test
    fun `newline in label`() {
        assertFailsWith<BranchLabelWhitespaceException> {
            globalProtocolInternal {
                send<Int>(a, b, "before\nafter")
            }
        }
    }

    @Test
    fun `dupe msg label`() {
        globalProtocol("Proto", callbacks = true) {
            send<String>(a, b, "my_label")
            send<String>(b, a, "my_label")
        }
    }

    @Test
    fun `dupe msg label 2`() {
        assertFailsWith<DuplicateMessageLabelsException> {
            globalProtocol("Proto", callbacks = true) {
                send<String>(a, b, "my_label")
                send<String>(a, b, "my_label")
            }
        }
    }

    @Test
    fun `dupe msg label exec 1`() {
        val aux: GlobalProtocol = {
            send<String>(a, b, "my_label")
        }
        assertFailsWith<DuplicateMessageLabelsException> {
            globalProtocol("Proto", callbacks = true) {
                send<String>(a, b, "my_label")
                aux()
            }
        }
    }

    @Test
    fun `dupe msg label exec 2`() {
        val aux: GlobalProtocol = {
            send<String>(a, b, "my_label")
        }
        assertFailsWith<DuplicateMessageLabelsException> {
            globalProtocol("Proto", callbacks = true) {
                aux()
                send<String>(a, b, "my_label")
            }
        }
    }

    @Test
    fun `dupe msg label choice`() {
        assertFailsWith<NonDeterministicStatesException> {
            globalProtocolInternal {
                choice(b) {
                    branch {
                        send<String>(b, a, "my_label")
                    }
                    branch {
                        send<Int>(b, a, "my_label")
                    }
                }
            }
        }
    }

    @Test
    fun `role declared inside choice`() {
        globalProtocolInternal {
            choice(b) {
                branch {
                    send<String>(b, a, "1")
                }
                branch {
                    send<Int>(b, a, "2")
                }
            }
        }
    }

    @Test
    fun `erasable recursion for one endpoint`() {
        val g = globalProtocolInternal {
            send<String>(a, b)

            // 'a' does nothing beyond this point
            send<String>(b, c)
            val t = mu()
            send<Int>(b, c)
            send<Int>(c, b)
            goto(t)
        }
        val lA = LocalTypeSend(b, StringClass, LEnd)
        assertEquals(lA, g.project(a))
    }

    @Test
    fun `space in role name`() {
        assertFailsWith<RoleNameWhitespaceException> {
            SKRole(" ")
        }
    }

    @Test
    fun `multiple spaces in role name`() {
        assertFailsWith<RoleNameWhitespaceException> {
            SKRole(" f ")
        }
    }

    @Test
    fun `tab in role name`() {
        assertFailsWith<RoleNameWhitespaceException> {
            SKRole("    f")
        }
    }

    @Test
    fun `newline in role name`() {
        assertFailsWith<RoleNameWhitespaceException> {
            SKRole("before\nafter")
        }
    }
}
