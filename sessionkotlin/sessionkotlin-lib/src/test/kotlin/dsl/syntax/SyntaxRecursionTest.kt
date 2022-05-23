package dsl.syntax

import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.TerminalInstructionException
import com.github.d_costa.sessionkotlin.dsl.exception.UndefinedRecursionVariableException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import dsl.util.IntClass
import dsl.util.LongClass
import dsl.util.StringClass
import dsl.util.UnitClass
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
            t = mu()
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
                val t = mu()
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
                val t = mu()
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
                val t = mu()
                send<Int>(a, b)
                send<Int>(b, a)
                goto(t)
                choice(c) {
                    branch("Case1") {
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
                goto(RecursionTag())
            }
        }
    }

    @Test
    fun `undefined recursion variable 2`() {
        assertFailsWith<UndefinedRecursionVariableException> {
            lateinit var t: RecursionTag
            globalProtocolInternal {
                t = mu()
            }

            globalProtocolInternal {
                send<Int>(a, b)
                choice(b) {
                    branch("1") {
                        send<Int>(b, c)
                        goto(t)
                    }
                    branch("2") {
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
                val t = mu()
                choice(b) {
                    branch("1") {
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

        val aux: GlobalProtocol = {
            t = mu()
        }

        val g = globalProtocolInternal {
            send<Int>(a, b)
            aux()
            choice(b) {
                branch("1") {
                    send<Int>(b, c)
                    send<Int>(b, a)
                    goto(t)
                }
                branch("2") {
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
        assertEquals(lC, g.project(c))
    }

    @Test
    fun `empty loop`() {
        val g = globalProtocolInternal {
            send<Int>(a, b)
            val t = mu()
            goto(t)
        }
        val lA = LocalTypeSend(b, IntClass, LEnd)
        val lB = LocalTypeReceive(a, IntClass, LEnd)
        assertEquals(lA, g.project(a))
        assertEquals(lB, g.project(b))
    }

    @Test
    fun `rec def after goto`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocolInternal {
                val t1 = mu()
                send<Unit>(a, b)
                goto(t1)

                // goto is a terminal operation
                val t2 = mu()
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
            t1 = mu()
            send<Unit>(a, b)
            choice(b) {
                branch("1") {
                    send<Int>(b, a)
                    goto(t1)
                }
                branch("2") {
                    send<Int>(b, a)
                    t2 = mu()
                    choice(a) {
                        branch("2.1") {
                            send<Int>(a, b)
                            goto(t2)
                        }
                        branch("2.2") {
                            send<Unit>(a, b)
                            goto(t1)
                        }
                    }
                }
                branch("3") {
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
                                        "2.1" to LocalTypeSend(b, IntClass, LocalTypeRecursion(t2), "2.1"),
                                        "2.2" to LocalTypeSend(b, UnitClass, LocalTypeRecursion(t1), "2.2")
                                    )
                                )
                            )
                        ),
                        "3" to LocalTypeReceive(b, StringClass, LEnd),

                    )
                )
            )
        )
        assertEquals(lA, g.project(a))
    }

    @Test
    fun `erasable choice goto`() {
        lateinit var t: RecursionTag
        val g = globalProtocolInternal {
            t = mu()
            send<Unit>(c, d)

            choice(a) {
                branch("1") {
                    send<Unit>(a, b)
                    // 'c' and 'd' can 'unpack' this choice
                    goto(t)
                }
            }
        }
        val lC = LocalTypeRecursionDefinition(t, LocalTypeSend(d, UnitClass, LocalTypeRecursion(t)))
        val lD = LocalTypeRecursionDefinition(t, LocalTypeReceive(c, UnitClass, LocalTypeRecursion(t)))
        assertEquals(lC, g.project(c))
        assertEquals(lD, g.project(d))
    }

    @Test
    fun `exec after goto`() {
        val aux: GlobalProtocol = {
            send<Int>(a, b)
        }
        assertFailsWith<TerminalInstructionException> {
            globalProtocolInternal {
                val t = mu()
                send<Int>(a, b)
                goto(t)
                // goto is a terminal instruction
                aux()
            }
        }
    }

    @Test
    fun `unused recursion variable Y`() {
        lateinit var x: RecursionTag

        val g = globalProtocolInternal {
            x = mu()
            send<Int>(a, b)
            choice(a) {
                branch("1") {
                    mu()
                    send<Long>(a, b)
                    goto(x)
                }
                branch("2") {
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
                            LocalTypeRecursion(x),
                            "1"
                        ),
                        "2" to LocalTypeSend(b, StringClass, LocalTypeEnd, "2")
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
        assertEquals(lA, g.project(a))
        assertEquals(lB, g.project(b))
    }

    @Test
    fun `unused recursion variables`() {
        val g = globalProtocolInternal {
            mu()
            choice(a) {
                branch("1") {
                    send<Long>(a, b)
                    mu()
                    send<Long>(b, a)
                }
            }
        }
        val lA = LocalTypeInternalChoice(
            mapOf(
                "1" to LocalTypeSend(b, LongClass, LocalTypeReceive(b, LongClass, LEnd), "1")
            )
        )
        val lB = LocalTypeExternalChoice(
            a,
            mapOf(
                "1" to LocalTypeReceive(a, LongClass, LocalTypeSend(a, LongClass, LEnd))
            )
        )
        assertEquals(lA, g.project(a))
        assertEquals(lB, g.project(b))
    }

    @Test
    fun `same rec label`() {
        globalProtocolInternal {
            val t1 = mu()
            send<Int>(a, b)
            val t2 = mu()
            choice(a) {
                branch("1") {
                    send<Long>(a, b)
                    goto(t1)
                }
                branch("2") {
                    send<String>(a, b)
                    goto(t2)
                }
            }
        }
    }

    @Test
    fun `empty recursions`() {
        globalProtocolInternal {
            mu() // unused
            send<Int>(a, b)
            send<Int>(a, c)
            // c can terminate here
            val t1 = mu()
            choice(a) {
                branch("1") {
                    send<Long>(a, b)
                }
                branch("2") {
                    send<String>(a, b)
                    goto(t1)
                }
            }
        }
    }

    @Test
    fun `empty recursions 2`() {
        val g = globalProtocolInternal {
            send<Int>(a, b)
            send<Int>(a, c)
            val t = mu()
            send<Int>(a, c)
            goto(t)
        }
        val lB = LocalTypeReceive(a, IntClass, LEnd)
        assertEquals(lB, g.project(b))
    }
}
