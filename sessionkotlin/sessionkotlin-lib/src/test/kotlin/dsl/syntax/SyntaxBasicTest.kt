package dsl.syntax

import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.*
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.LEnd
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeExternalChoice
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeReceive
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeSend
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
                branch("Case1") {
                    send<String>(b, a)
                    send<String>(b, a)
                }
                branch("Case2") {
                    choice(b) {
                        branch("SubCase1") {
                            send<Long>(b, a)
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
                branch("Ok") {
                    send<String>(b, s)
                    send<String>(s, b)
                }
                branch("Quit") {
                    send<String>(b, s)
                }
            }
        }
    }

    @Test
    fun `send after choice`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocolInternal {
                choice(a) {
                    branch("Case1") {
                        send<Unit>(a, b)
                    }
                    branch("Case2") {
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
        assertFailsWith<DuplicateBranchLabelException> {
            globalProtocolInternal {
                choice(b) {
                    branch("Case1") {
                        send<String>(b, a)
                    }
                    branch("Case1") {
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
                branch("Case1") {
                    send<String>(b, a)
                }
                branch("Case2") {
                    send<Int>(b, a)
                    send<Long>(a, b)
                    choice(a) {
                        branch("Case2") {
                            send<Int>(a, b)
                        }
                        branch("Case1") {
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
                branch("Case1") {
                    send<Int>(b, c)
                    send<String>(b, a)
                }
                branch("Case2") {
                    send<String>(b, a)
                    send<Int>(b, c)
                }
            }
        }
    }

    @Test
    fun `different actual choice subject`() {
        val g = globalProtocolInternal {
            choice(b) {
                branch("1") {
                    send<String>(b, c)
                    send<String>(c, a)
                }
                branch("2") {
                    send<Int>(b, c)
                    send<Int>(c, a)
                }
            }
        }
        val lA = LocalTypeExternalChoice(
            c,
            mapOf(
                "1" to LocalTypeReceive(c, StringClass, LEnd),
                "2" to LocalTypeReceive(c, IntClass, LEnd)
            )
        )
        assertEquals(g.project(a), lA)
    }

    @Test
    fun `different actual choice subject 2`() {
        val g = globalProtocolInternal {
            choice(b) {
                branch("1") {
                    send<Int>(b, c)
                    send<Int>(c, a)
                }
                branch("2") {
                    send<Int>(b, c)
                    send<Int>(c, a)
                }
            }
        }
        val lA = LocalTypeExternalChoice(
            c,
            mapOf(
                "1" to LocalTypeReceive(c, IntClass, LEnd),
                "2" to LocalTypeReceive(c, IntClass, LEnd)
            )
        )
        assertEquals(g.project(a), lA)
    }

    @Test
    fun `space in label`() {
        assertFailsWith<InvalidBranchLabelException> {
            globalProtocolInternal {
                choice(a) {
                    branch(" Case1") {
                    }
                }
            }
        }
    }

    @Test
    fun `multiple spaces in label`() {
        assertFailsWith<InvalidBranchLabelException> {
            globalProtocolInternal {
                choice(a) {
                    branch("C ase 1") {
                    }
                }
            }
        }
    }

    @Test
    fun `tab in label`() {
        assertFailsWith<InvalidBranchLabelException> {
            globalProtocolInternal {
                choice(a) {
                    branch("before\tafter") {
                    }
                }
            }
        }
    }

    @Test
    fun `newline in label`() {
        assertFailsWith<InvalidBranchLabelException> {
            globalProtocolInternal {
                choice(a) {
                    branch("before\nafter") {
                    }
                }
            }
        }
    }

    @Test
    fun `dupe msg label`() {
        assertFailsWith<DuplicateMessageLabelException> {
            globalProtocolInternal {
                send<String>(a, b, "my_label")
                send<String>(b, a, "my_label")
            }
        }
    }

    @Test
    fun `dupe msg label exec 1`() {
        val aux: GlobalProtocol = {
            send<String>(a, b, "my_label")
        }
        assertFailsWith<DuplicateMessageLabelException> {
            globalProtocolInternal {
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
        assertFailsWith<DuplicateMessageLabelException> {
            globalProtocolInternal {
                aux()
                send<String>(a, b, "my_label")
            }
        }
    }

    @Test
    fun `dupe msg label choice`() {
        assertFailsWith<DuplicateMessageLabelException> {
            globalProtocolInternal {
                choice(b) {
                    branch("1") {
                        send<String>(b, a, "my_label")
                    }
                    branch("2") {
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
                branch("1") {
                    send<String>(b, a)
                }
                branch("2") {
                    send<Int>(b, a)
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
            val t = miu()
            send<Int>(b, c)
            send<Int>(c, b)
            goto(t)
        }
        val lA = LocalTypeSend(b, StringClass, LEnd)
        assertEquals(lA, g.project(a))
    }
}
