package dsl.syntax

import dsl.util.IntClass
import dsl.util.LongClass
import dsl.util.StringClass
import dsl.util.UnitClass
import org.david.sessionkotlin_lib.dsl.RecursionTag
import org.david.sessionkotlin_lib.dsl.SKRole
import org.david.sessionkotlin_lib.dsl.exception.TerminalInstructionException
import org.david.sessionkotlin_lib.dsl.exception.UndefinedRecursionVariableException
import org.david.sessionkotlin_lib.dsl.globalProtocolInternal
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyntaxRecursionTest {

    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
        val d = SKRole("D")
    }

    @Test
    fun `basic recursion`() {
        lateinit var t: RecursionTag
        val g = globalProtocolInternal {
            t = miu("X")
            send<Int>(a, b)
            send<Int>(b, a)
            goto(t)
        }
        val lA = LocalTypeRecursionDefinition(
            t,
            LocalTypeSend(b, IntClass, LocalTypeReceive(b, IntClass, LocalTypeRecursion(t)))
        )
        assertEquals(g.project(a), lA)
    }

    @Test
    fun `send after goto`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocolInternal {
                val t = miu("X")
                send<Int>(a, b)
                send<Int>(b, a)
                goto(t)
                send<Int>(b, c)
            }
        }
    }

    @Test
    fun `choice after goto`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocolInternal {
                val t = miu("X")
                send<Int>(a, b)
                send<Int>(b, a)
                goto(t)
                choice(b) {}
            }
        }
    }

    @Test
    fun `send after goto 2`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocolInternal {
                val t = miu("X")
                send<Int>(a, b)
                send<Int>(b, a)
                goto(t)
                choice(c) {
                    case("Case1") {
                        send<Int>(c, b)
                    }
                }
            }
        }
    }

    @Test
    fun `undefined recursion variable`() {
        assertFailsWith<UndefinedRecursionVariableException> {
            globalProtocolInternal {
                goto(RecursionTag("X"))
            }
        }
    }

    @Test
    fun `undefined recursion variable 2`() {
        assertFailsWith<UndefinedRecursionVariableException> {
            lateinit var t: RecursionTag
            globalProtocolInternal {
                t = miu("X")
            }

            globalProtocolInternal {
                send<Int>(a, b)
                choice(b) {
                    case("1") {
                        send<Int>(b, c)
                        goto(t)
                    }
                    case("2") {
                        send<Int>(b, c)
                    }
                }
            }
        }
    }

    @Test
    fun `send after goto inside choice`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocolInternal {
                val t = miu("X")
                choice(b) {
                    case("1") {
                        goto(t)
                        send<Int>(b, a)
                    }
                }
            }
        }
    }

    @Test
    fun `recursion variable defined in subprotocol`() {
        lateinit var t: RecursionTag

        val aux = globalProtocolInternal {
            t = miu("X")
        }

        val g = globalProtocolInternal {
            send<Int>(a, b)
            exec(aux)
            choice(b) {
                case("1") {
                    send<Int>(b, c)
                    send<Int>(b, a)
                    goto(t)
                }
                case("2") {
                    send<Int>(b, a)
                    send<Int>(b, c)
                }
            }
        }
        val lC = LocalTypeRecursionDefinition(
            t,
            LocalTypeExternalChoice(
                b,
                mapOf(
                    "1" to LocalTypeReceive(b, IntClass, LocalTypeRecursion(t)),
                    "2" to LocalTypeReceive(b, IntClass, LEnd)
                )
            )
        )
        assertEquals(g.project(c), lC)
    }

    @Test
    fun `empty loop`() {
        val g = globalProtocolInternal {
            send<Int>(a, b)
            val t = miu("X")
            goto(t)
        }
        val lA = LocalTypeSend(b, IntClass, LEnd)
        val lB = LocalTypeReceive(a, IntClass, LEnd)
        assertEquals(g.project(a), lA)
        assertEquals(g.project(b), lB)
    }

    @Test
    fun `rec def after goto`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocolInternal {
                val t1 = miu("X")
                send<Unit>(a, b)
                goto(t1)

                // goto is a terminal operation
                val t2 = miu("Y")
                send<Unit>(b, a)
                goto(t2)
            }
        }
    }

    @Test
    fun `nested recursion`() {
        lateinit var t1: RecursionTag
        lateinit var t2: RecursionTag
        val g = globalProtocolInternal {
            t1 = miu("X")
            send<Unit>(a, b)
            choice(b) {
                case("1") {
                    send<Int>(b, a)
                    goto(t1)
                }
                case("2") {
                    send<Int>(b, a)
                    t2 = miu("Y")
                    choice(a) {
                        case("2.1") {
                            send<Int>(a, b)
                            goto(t2)
                        }
                        case("2.2") {
                            send<Unit>(a, b)
                            goto(t1)
                        }
                    }
                }
                case("3") {
                    send<String>(b, a)
                }
            }
        }
        val lA = LocalTypeRecursionDefinition(
            t1,
            LocalTypeSend(
                b, UnitClass,
                LocalTypeExternalChoice(
                    b,
                    mapOf(
                        "1" to LocalTypeReceive(b, IntClass, LocalTypeRecursion(t1)),
                        "2" to LocalTypeReceive(
                            b, IntClass,
                            LocalTypeRecursionDefinition(
                                t2,
                                LocalTypeInternalChoice(
                                    mapOf(
                                        "2.1" to LocalTypeSend(b, IntClass, LocalTypeRecursion(t2)),
                                        "2.2" to LocalTypeSend(b, UnitClass, LocalTypeRecursion(t1))
                                    )
                                )
                            )
                        ),
                        "3" to LocalTypeReceive(b, StringClass, LEnd),

                    )
                )
            )
        )
        assertEquals(g.project(a), lA)
    }

    @Test
    fun `erasable choice goto`() {
        lateinit var t: RecursionTag
        val g = globalProtocolInternal {
            t = miu("X")
            send<Unit>(c, d)

            choice(a) {
                case("1") {
                    send<Unit>(a, b)
                    // 'c' and 'd' can 'unpack' this choice
                    goto(t)
                }
            }
        }
        val lC = LocalTypeRecursionDefinition(t, LocalTypeSend(d, UnitClass, LocalTypeRecursion(t)))
        val lD = LocalTypeRecursionDefinition(t, LocalTypeReceive(c, UnitClass, LocalTypeRecursion(t)))
        assertEquals(g.project(c), lC)
        assertEquals(g.project(d), lD)
    }

    @Test
    fun `exec after goto`() {
        val aux = globalProtocolInternal {
            send<Int>(a, b)
        }
        assertFailsWith<TerminalInstructionException> {
            globalProtocolInternal {
                val t = miu("X")
                send<Int>(a, b)
                goto(t)
                // goto is a terminal instruction
                exec(aux)
            }
        }
    }

    @Test
    fun `unused recursion variable Y`() {
        lateinit var x: RecursionTag

        val g = globalProtocolInternal {
            x = miu("X")
            send<Int>(a, b)
            choice(a) {
                case("1") {
                    miu("Y")
                    send<Long>(a, b)
                    goto(x)
                }
                case("2") {
                    send<String>(a, b)
                }
            }
        }
        val lA = LocalTypeRecursionDefinition(
            x,
            LocalTypeSend(
                b,
                IntClass,
                LocalTypeInternalChoice(
                    mapOf(
                        "1" to LocalTypeSend(
                            b,
                            LongClass,
                            LocalTypeRecursion(x)
                        ),
                        "2" to LocalTypeSend(b, StringClass, LocalTypeEnd)
                    )
                )
            )
        )
        val lB = LocalTypeRecursionDefinition(
            x,
            LocalTypeReceive(
                a,
                IntClass,
                LocalTypeExternalChoice(
                    a,
                    mapOf(
                        "1" to LocalTypeReceive(a, LongClass, LocalTypeRecursion(x)),
                        "2" to LocalTypeReceive(a, StringClass, LocalTypeEnd)
                    )
                )
            )
        )
        assertEquals(g.project(a), lA)
        assertEquals(g.project(b), lB)
    }

    @Test
    fun `unused recursion variables`() {
        val g = globalProtocolInternal {
            miu("X")
            choice(a) {
                case("1") {
                    send<Long>(a, b)
                    miu("Y")
                    send<Long>(b, a)
                }
            }
        }
        val lA = LocalTypeInternalChoice(
            mapOf(
                "1" to LocalTypeSend(b, LongClass, LocalTypeReceive(b, LongClass, LEnd))
            )
        )
        val lB = LocalTypeExternalChoice(
            a,
            mapOf(
                "1" to LocalTypeReceive(a, LongClass, LocalTypeSend(a, LongClass, LEnd))
            )
        )
        assertEquals(g.project(a), lA)
        assertEquals(g.project(b), lB)
    }

    @Test
    fun `same rec label`() {
        globalProtocolInternal {
            val t1 = miu("X")
            send<Int>(a, b)
            val t2 = miu("X")
            choice(a) {
                case("1") {
                    send<Long>(a, b)
                    goto(t1)
                }
                case("2") {
                    send<String>(a, b)
                    goto(t2)
                }
            }
        }
    }
}
