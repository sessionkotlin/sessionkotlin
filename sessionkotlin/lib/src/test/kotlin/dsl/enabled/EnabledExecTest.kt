package dsl.enabled

import com.github.sessionkotlin.lib.dsl.GlobalProtocol
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.exception.RoleNotEnabledException
import com.github.sessionkotlin.lib.dsl.exception.UnfinishedRolesException
import com.github.sessionkotlin.lib.dsl.globalProtocolInternal
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
        val x: GlobalProtocol = {
            // 'c' not enabled
            send<Int>(c, b)
        }
        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                send<Int>(a, b)
                choice(b) {
                    branch {
                        x()
                    }
                    branch {
                        send<Int>(b, c)
                        send<Int>(c, b)
                    }
                }
            }
        }
    }

    @Test
    fun `activated in exec`() {
        val aux: GlobalProtocol = {
            send<Int>(b, c)
        }

        globalProtocolInternal {
            choice(b) {
                branch {
                    aux()
                    // 'c' was enabled inside aux
                    send<Int>(c, a)
                }
            }
        }
    }

    @Test
    fun `not enabled in exec after map`() {
        fun subProtocol(x: SKRole, y: SKRole): GlobalProtocol = {
            send<Int>(x, y)
            send<Int>(y, x)
        }

        val branch1 = subProtocol(b, a)
        val branch2 = subProtocol(a, b)

        assertFailsWith<RoleNotEnabledException> {
            globalProtocolInternal {
                choice(a) {
                    branch {
                        // b not enabled
                        branch1()
                    }
                    branch {
                        branch2()
                    }
                }
            }
        }
    }
}
