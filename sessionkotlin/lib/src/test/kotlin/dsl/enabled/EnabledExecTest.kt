package dsl.enabled

import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin.dsl.exception.UnfinishedRolesException
import org.david.sessionkotlin.dsl.globalProtocolInternal
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class EnabledExecTest {
    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
        val d = SKRole("D")
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
                    branch("Case1") {
                        exec(x)
                    }
                    branch("Case2") {
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
                branch("Case1") {
                    exec(aux)
                    // 'c' was enabled inside aux
                    send<Int>(c, a)
                }
            }
        }
    }

    @Test
    fun `not enabled in exec after map`() {
        val x = SKRole("X")
        val y = SKRole("Y")
        val subprotocol = globalProtocolInternal {
            send<Int>(x, y)
            send<Int>(y, x)
        }
        assertFailsWith<RoleNotEnabledException> {
            globalProtocolInternal {
                choice(a) {
                    branch("1") {
                        // b not enabled
                        exec(subprotocol, mapOf(x to b, y to a))
                    }
                    branch("2") {
                        exec(subprotocol, mapOf(x to a, y to b))
                    }
                }
            }
        }
    }
}
