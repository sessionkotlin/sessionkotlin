package dsl

import org.david.sessionkotlin_lib.dsl.RecursionTag
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.Samples
import org.david.sessionkotlin_lib.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin_lib.dsl.exception.TerminalInstructionException
import org.david.sessionkotlin_lib.dsl.exception.UndefinedRecursionVariableException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class RecTest {

    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
    }

    @Test
    fun `normal rec`() {
        globalProtocol {
            val t = miu("X")
            send<Int>(a, b)
            send<Int>(b, a)
            goto(t)
        }
    }

    @Test
    fun `illegal rec`() {
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
    fun `illegal rec reversed`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
                val t = miu("X")
                send<Int>(a, b)
                send<Int>(b, a)
                goto(t)
                send<Int>(c, b)
            }
        }
    }

    @Test
    fun `illegal rec choice at`() {
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
    fun `illegal rec choice at 2`() {
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
    fun `illegal rec choice case`() {
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
    fun `illegal rec choice case 2`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
                val t = miu("X")
                send<Int>(a, b)
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
    fun `test rec example`() {
        Samples().goto()
    }


    @Test
    fun `rec disabled role`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocol {
                val t = miu("X")
                send<Int>(a, b)
                send<Int>(c, b)
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
    fun `undefined rec variable`() {
        assertFailsWith<UndefinedRecursionVariableException> {
            globalProtocol {
                send<Int>(a, b)
                send<Int>(c, b)
                choice(b) {
                    case("1") {
                        send<Int>(b, c)
                        goto(RecursionTag("X"))
                    }
                    case("2") {
                        send<Int>(b, c)
                    }
                }
            }
        }
    }

    @Test
    fun `undefined rec variable 2`() {
        assertFailsWith<UndefinedRecursionVariableException> {
            lateinit var t: RecursionTag
            globalProtocol {
                t = miu("X")
            }
            globalProtocol {
                send<Int>(a, b)
                send<Int>(c, b)
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
    fun `empty loop`() {
        globalProtocol {
            send<Int>(a, b)
            send<Int>(c, b)
            val t = miu("X")
            goto(t)
        }
    }
}