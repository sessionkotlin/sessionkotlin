package dsl

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.Samples
import org.david.sessionkotlin_lib.dsl.exception.RecursiveProtocolException
import org.david.sessionkotlin_lib.dsl.exception.RoleNotEnabledException
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
            send<Int>(a, b)
            send<Int>(b, a)
            rec()
        }
    }

    @Test
    fun `illegal rec`() {
        assertFailsWith<RecursiveProtocolException> {
            globalProtocol {
                send<Int>(a, b)
                send<Int>(b, a)
                rec()
                send<Int>(b, c)
            }
        }
    }

    @Test
    fun `illegal rec reversed`() {
        assertFailsWith<RecursiveProtocolException> {
            globalProtocol {
                send<Int>(a, b)
                send<Int>(b, a)
                rec()
                send<Int>(c, b)
            }
        }
    }

    @Test
    fun `illegal rec choice at`() {
        assertFailsWith<RecursiveProtocolException> {
            globalProtocol {
                send<Int>(a, b)
                send<Int>(b, a)
                rec()
                choice(b) {}
            }
        }
    }


    @Test
    fun `illegal rec choice at 2`() {
        assertFailsWith<RecursiveProtocolException> {
            globalProtocol {
                choice(b) {
                    case("1") {
                        rec()
                        send<Int>(b, a)
                    }
                }
            }
        }
    }

    @Test
    fun `illegal rec choice case`() {
        assertFailsWith<RecursiveProtocolException> {
            globalProtocol {
                send<Int>(a, b)
                send<Int>(b, a)
                rec()
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
        assertFailsWith<RecursiveProtocolException> {
            globalProtocol {
                send<Int>(a, b)
                rec()
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
        Samples().rec()
    }


    @Test
    fun `rec disabled role`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocol {
                send<Int>(a, b)
                send<Int>(c, b)
                choice(b) {
                    case("1") {
                        send<Int>(b, c)
                        rec()
                    }
                    case("2") {
                        send<Int>(b, c)
                    }
                }
            }
        }
    }
}