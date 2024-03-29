package dsl.syntax

import com.github.sessionkotlin.lib.dsl.GlobalProtocol
import com.github.sessionkotlin.lib.dsl.RecursionTag
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.exception.TerminalInstructionException
import com.github.sessionkotlin.lib.dsl.exception.UndefinedRecursionVariableException
import com.github.sessionkotlin.lib.dsl.globalProtocolInternal
import com.github.sessionkotlin.lib.dsl.types.*
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
                    branch {
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
                    branch {
                        send<Int>(b, c)
                        goto(t)
                    }
                    branch {
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
                    branch {
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
                branch {
                    send<Int>(b, c, "1")
                    send<Int>(b, a, "1")
                    goto(t)
                }
                branch {
                    send<Int>(b, a, "2")
                    send<Int>(b, c, "2")
                }
            }
        }
        val lC = LocalTypeRecursionDefinition(
            t,
            LocalTypeExternalChoice(
                b,
                listOf(
                    LocalTypeReceive(b, IntClass, MsgLabel("1"), LocalTypeRecursion(t)),
                    LocalTypeReceive(b, IntClass, MsgLabel("2"), LEnd)
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
                branch {
                    send<Int>(b, a, "1")
                    goto(t1)
                }
                branch {
                    send<Int>(b, a, "2")
                    t2 = mu()
                    choice(a) {
                        branch {
                            send<Int>(a, b, "21")
                            goto(t2)
                        }
                        branch {
                            send<Unit>(a, b, "22")
                            goto(t1)
                        }
                    }
                }
                branch {
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
                    listOf(
                        LocalTypeReceive(b, IntClass, MsgLabel("1"), LocalTypeRecursion(t1)),
                        LocalTypeReceive(
                            b, IntClass, MsgLabel("2"),
                            LocalTypeRecursionDefinition(
                                t2,
                                LocalTypeInternalChoice(
                                    listOf(
                                        LocalTypeSend(b, IntClass, MsgLabel("21"), LocalTypeRecursion(t2)),
                                        LocalTypeSend(b, UnitClass, MsgLabel("22"), LocalTypeRecursion(t1))
                                    )
                                )
                            )
                        ),
                        LocalTypeReceive(b, StringClass, LEnd),

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
                branch {
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
                branch {
                    mu()
                    send<Long>(a, b, "rec")
                    goto(x)
                }
                branch {
                    send<String>(a, b, "quit")
                }
            }
        }
        val lA = LocalTypeRecursionDefinition(
            x,
            LocalTypeSend(
                b,
                IntClass,
                LocalTypeInternalChoice(
                    listOf(
                        LocalTypeSend(
                            b,
                            LongClass,
                            MsgLabel("rec"),
                            LocalTypeRecursion(x)
                        ),
                        LocalTypeSend(
                            b, StringClass, MsgLabel("quit"),
                            LocalTypeEnd
                        )
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
                    listOf(
                        LocalTypeReceive(a, LongClass, MsgLabel("rec"), LocalTypeRecursion(x)),
                        LocalTypeReceive(a, StringClass, MsgLabel("quit"), LocalTypeEnd)
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
                branch {
                    send<Long>(a, b)
                    mu()
                    send<Long>(b, a)
                }
            }
        }
        val lA = LocalTypeInternalChoice(
            listOf(
                LocalTypeSend(b, LongClass, LocalTypeReceive(b, LongClass, LEnd))
            )
        )
        val lB = LocalTypeReceive(a, LongClass, LocalTypeSend(a, LongClass, LEnd))

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
                branch {
                    send<Long>(a, b, "b1")
                    goto(t1)
                }
                branch {
                    send<String>(a, b, "b2")
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
                branch {
                    send<Long>(a, b, "b1")
                }
                branch {
                    send<String>(a, b, "b2")
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
