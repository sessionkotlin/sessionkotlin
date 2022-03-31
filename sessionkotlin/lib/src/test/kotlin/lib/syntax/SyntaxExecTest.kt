package lib.syntax

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.SendingtoSelfException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyntaxExecTest {

    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
        val d = Role("D")
    }


    @Test
    fun `basic exec`() {
        val x = globalProtocol {
            send<Int>(b, c)
        }

        globalProtocol {
            send<Int>(a, b)
            exec(x)
        }
    }

    @Test
    fun `basic exec new roles`() {
        val x = globalProtocol {
            send<Int>(c, a)
        }

        globalProtocol {
            send<Int>(a, b)
            exec(x)
        }
    }

    @Test
    fun `same role sending and receiving 2`() {
        assertFailsWith<SendingtoSelfException> {
            val x = globalProtocol {
                send<Int>(b, c)
            }
            globalProtocol {
                send<Int>(a, b)
                exec(x, mapOf(c to b))
            }
        }
    }

    @Test
    fun `same role sending and receiving 3`() {
        assertFailsWith<SendingtoSelfException> {
            val x = globalProtocol {
                choice(a) {
                    case("1") {
                        send<Int>(a, b)
                    }
                }
            }

            globalProtocol {
                send<Int>(a, b)
                exec(x, mapOf(a to b))
            }
        }
    }

    @Test
    fun `roles in map but not in protocol`() {
        val subprotocol = globalProtocol {
            send<Int>(a, c)
        }
        val x = Role("X")

        val g = globalProtocol {
            choice(a) {
                case("1") {
                    exec(subprotocol)
                }
                case("2") {
                    // 'x' to 'c' is ignored
                    exec(subprotocol, mapOf(x to c))
                }
            }
        }
        assert(!g.roles.contains(x))
    }

    @Test
    fun `reversed roles`() {
        val subprotocol = globalProtocol {
            choice(a) {
                case("1") {
                    send<String>(a, b)
                    send<String>(b, a)
                }
                case("2") {
                    send<Unit>(a, b)
                }
            }

        }
        val g = globalProtocol {
            exec(subprotocol, mapOf(a to b, b to a)) // reverse roles
        }

        val lB = LocalTypeInternalChoice(mapOf("1" to LocalTypeSend(a,
            String::class.java,
            LocalTypeReceive(a, String::class.java, LocalTypeEnd)),
            "2" to LocalTypeSend(a, Unit::class.java, LocalTypeEnd)))
        val lA = LocalTypeExternalChoice(b,
            mapOf("1" to LocalTypeReceive(b, String::class.java, LocalTypeSend(b, String::class.java, LocalTypeEnd)),
                "2" to LocalTypeReceive(b, Unit::class.java, LocalTypeEnd)))
        assertEquals(g.project(a), lA)
        assertEquals(g.project(b), lB)
    }
}
