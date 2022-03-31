package lib.unfinished

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.UnfinishedRolesException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
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
        globalProtocol {
            choice(a) {
                case("1") {
                    exec(aux)
                }
                case("2") {
                    exec(aux)
                }
                // branches mergeable for 'c', even without being activated
            }
        }
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