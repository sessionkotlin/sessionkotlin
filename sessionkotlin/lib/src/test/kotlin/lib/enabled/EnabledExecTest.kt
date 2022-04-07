package lib.enabled

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin_lib.dsl.exception.UnfinishedRolesException
import org.david.sessionkotlin_lib.dsl.globalProtocolInternal
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
        val x = globalProtocolInternal {
            // 'c' not enabled
            send<Int>(c, b)
        }
        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                send<Int>(a, b)
                choice(b) {
                    case("Case1") {
                        exec(x)
                    }
                    case("Case2") {
                        send<Int>(b, c)
                        send<Int>(c, b)
                    }
                }
            }
        }
    }

    @Test
    fun `activated in exec`() {
        val aux = globalProtocolInternal {
            send<Int>(b, c)
        }

        globalProtocolInternal {
            choice(b) {
                case("Case1") {
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
        val subprotocol = globalProtocolInternal {
            send<Int>(x, y)
            send<Int>(y, x)
        }
        assertFailsWith<RoleNotEnabledException> {
            globalProtocolInternal {
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
