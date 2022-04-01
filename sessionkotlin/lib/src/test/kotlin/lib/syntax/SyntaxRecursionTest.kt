package lib.syntax

import org.david.sessionkotlin_lib.dsl.RecursionTag
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.TerminalInstructionException
import org.david.sessionkotlin_lib.dsl.exception.UndefinedRecursionVariableException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyntaxRecursionTest {

    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
        val d = Role("D")
    }

    @Test
    fun `basic recursion`() {
        globalProtocol {
            val t = miu("X")
            send<Int>(a, b)
            send<Int>(b, a)
            goto(t)
        }
    }

    @Test
    fun `send after goto`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
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
            globalProtocol {
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
            globalProtocol {
                val t = miu("X")
                send<Int>(a, b)
                send<Int>(b, a)
                goto(t)
                choice(c) {
                    case("Case 1") {
                        send<Int>(c, b)
                    }
                }
            }
        }
    }

    @Test
    fun `undefined recursion variable`() {
        assertFailsWith<UndefinedRecursionVariableException> {
            globalProtocol {
                goto(RecursionTag("X"))
            }
        }
    }

    @Test
    fun `undefined recursion variable 2`() {
        assertFailsWith<UndefinedRecursionVariableException> {
            lateinit var t: RecursionTag
            globalProtocol {
                t = miu("X")
            }

            globalProtocol {
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
            globalProtocol {
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

        val aux = globalProtocol {
            t = miu("X")
        }

        globalProtocol {
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
    }

    @Test
    fun `empty loop`() {
        globalProtocol {
            send<Int>(a, b)
            val t = miu("X")
            goto(t)
        }
    }

    @Test
    fun `rec def after goto`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
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
        globalProtocol {
            val t1 = miu("X")
            send<Unit>(a, b)
            choice(b) {
                case("1") {
                    send<Int>(b, a)
                    goto(t1)
                }
                case("2") {
                    send<Int>(b, a)
                    val t2 = miu("Y")
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
    }

    @Test
    fun `erasable choice goto`() {
        lateinit var t: RecursionTag
        val g = globalProtocol {
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
        val lC = LocalTypeRecursionDefinition(t, LocalTypeSend(d, Unit::class.java, LocalTypeRecursion(t)))

        assertEquals(g.project(c), lC)
    }

    @Test
    fun `exec after goto`() {
        val aux = globalProtocol {
            send<Int>(a, b)
        }
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
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

        val g = globalProtocol {
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
                Int::class.javaObjectType,
                LocalTypeInternalChoice(
                    mapOf(
                        "1" to LocalTypeSend(
                            b,
                            Long::class.javaObjectType,
                            LocalTypeRecursion(x)
                        ),
                        "2" to LocalTypeSend(b, String::class.java, LocalTypeEnd)
                    )
                )
            )
        )
        val lB = LocalTypeRecursionDefinition(
            x,
            LocalTypeReceive(
                a,
                Int::class.javaObjectType,
                LocalTypeExternalChoice(
                    a,
                    mapOf(
                        "1" to LocalTypeReceive(a, Long::class.javaObjectType, LocalTypeRecursion(x)),
                        "2" to LocalTypeReceive(a, String::class.java, LocalTypeEnd)
                    )
                )
            )
        )
        assertEquals(g.project(a), lA)
        assertEquals(g.project(b), lB)
    }

    @Test
    fun `unused recursion variables`() {
        globalProtocol {
            miu("X")
            choice(a) {
                case("1") {
                    send<Long>(a, b)
                    miu("Y")
                    send<Long>(b, a)
                }
            }
        }
    }
}
