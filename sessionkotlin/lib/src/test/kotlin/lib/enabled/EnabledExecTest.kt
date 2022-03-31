package lib.enabled

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin_lib.dsl.exception.UnfinishedRolesException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class EnabledExecTest {
    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
        val d = Role("D")
    }

    @Test
    fun `exec not enabled`() {
        val x = globalProtocol {
            // 'c' not enabled
            send<Int>(c, b)
        }
        assertFailsWith<UnfinishedRolesException> {
            globalProtocol {
                send<Int>(a, b)
                choice(b) {
                    case("Case1") {
                        exec(x)
                    }
                    case("Case 2") {
                        send<Int>(b, c)
                        send<Int>(c, b)
                    }
                }
            }
        }
    }

    @Test
    fun `activated in exec`() {
        val aux = globalProtocol {
            send<Int>(b, c)
        }

        globalProtocol {
            choice(b) {
                case("Case 1") {
                    exec(aux)
                    // 'c' was enabled inside aux
                    send<Int>(c, a)
                }
            }
        }
    }

    @Test
    fun `not enabled in exec after map`() {
        val x = Role("X")
        val y = Role("Y")
        val subprotocol = globalProtocol {
            send<Int>(x, y)
            send<Int>(y, x)
        }
        assertFailsWith<RoleNotEnabledException> {
            globalProtocol {
                choice(a) {
                    case("1") {
                        // b not enabled
                        exec(subprotocol, mapOf(x to b, y to a))
                    }
                    case("2") {
                        exec(subprotocol, mapOf(x to a, y to b))
                    }
                }
            }
        }
    }

}