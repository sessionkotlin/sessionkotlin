package lib.syntax

import lib.util.DoubleClass
import lib.util.IntClass
import lib.util.StringClass
import org.david.sessionkotlin_lib.dsl.SKRole
import org.david.sessionkotlin_lib.dsl.exception.DuplicateCaseLabelException
import org.david.sessionkotlin_lib.dsl.exception.SendingtoSelfException
import org.david.sessionkotlin_lib.dsl.exception.TerminalInstructionException
import org.david.sessionkotlin_lib.dsl.globalProtocolInternal
import org.david.sessionkotlin_lib.dsl.types.LEnd
import org.david.sessionkotlin_lib.dsl.types.LocalTypeExternalChoice
import org.david.sessionkotlin_lib.dsl.types.LocalTypeReceive
import org.david.sessionkotlin_lib.dsl.types.LocalTypeSend
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
        assertFailsWith<SendingtoSelfException> {
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
                case("Case1") {
                    send<String>(b, a)
                    send<String>(b, a)
                }
                case("Case2") {
                    choice(b) {
                        case("SubCase1") {
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
                case("Ok") {
                    send<String>(b, s)
                    send<String>(s, b)
                }
                case("Quit") {
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
                    case("Case1") {
                        send<Unit>(a, b)
                    }
                    case("Case2") {
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
        assertFailsWith<DuplicateCaseLabelException> {
            globalProtocolInternal {
                choice(b) {
                    case("Case1") {
                        send<String>(b, a)
                    }
                    case("Case1") {
                        send<Int>(b, a)
                        send<Long>(a, b)
                    }
                }
            }
        }
    }

    @Test
    fun `dupe case labels nested`() {
        globalProtocolInternal {
            choice(b) {
                case("Case1") {
                    send<String>(b, a)
                }
                case("Case2") {
                    send<Int>(b, a)
                    send<Long>(a, b)
                    choice(a) {
                        case("Case2") {
                            send<Int>(a, b)
                        }
                        case("Case1") {
                            send<Int>(a, b)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `cumutative sends in choice branches`() {
        globalProtocolInternal {
            choice(b) {
                case("Case1") {
                    send<Int>(b, c)
                    send<String>(b, a)
                }
                case("Case2") {
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
                case("1") {
                    send<String>(b, c)
                    send<String>(c, a)
                }
                case("2") {
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
                case("1") {
                    send<Int>(b, c)
                    send<Int>(c, a)
                }
                case("2") {
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
}
