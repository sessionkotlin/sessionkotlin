package lib.unfinished

import lib.util.IntClass
import lib.util.LongClass
import lib.util.StringClass
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.UnfinishedRolesException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.david.sessionkotlin_lib.dsl.types.LEnd
import org.david.sessionkotlin_lib.dsl.types.LocalTypeReceive
import org.david.sessionkotlin_lib.dsl.types.LocalTypeSend
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UnfinishedExecTest {
    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
        val d = Role("D")
    }

    @Test
    fun `choice agnostic`() {
        val aux = globalProtocol {
            send<String>(b, c)
            send<Int>(a, b)
            send<Long>(b, c)
        }
        val g = globalProtocol {
            choice(a) {
                case("1") {
                    exec(aux)
                }
                case("2") {
                    exec(aux)
                }
                // branches mergeable for 'b', even without being activated for the first send
            }
        }
        val lB = LocalTypeSend(
            c, StringClass,
            LocalTypeReceive(
                a, IntClass,
                LocalTypeSend(c, LongClass, LEnd)
            )
        )
        assertEquals(g.project(b), lB)
    }

    @Test
    fun `unfinished after map`() {
        val subprotocol = globalProtocol {
            send<Int>(a, b)
            send<Int>(a, c)
        }

        assertFailsWith<UnfinishedRolesException> {
            globalProtocol {
                send<Int>(a, b)
                send<Int>(a, c)
                choice(a) {
                    case("1") {
                        exec(subprotocol, mapOf(a to b, b to a))
                    }
                    case("2") {
                        exec(subprotocol)
                    }
                }
            }
        }
    }

    @Test
    fun `unfinished after map 2`() {
        val subprotocol = globalProtocol {
            send<Int>(a, b)
            send<Int>(a, c)
        }

        assertFailsWith<UnfinishedRolesException> {
            globalProtocol {
                choice(a) {
                    case("1") {
                        exec(subprotocol)
                    }
                    case("2") {
                        exec(subprotocol, mapOf(b to c))
                    }
                }
            }
        }
    }
}
