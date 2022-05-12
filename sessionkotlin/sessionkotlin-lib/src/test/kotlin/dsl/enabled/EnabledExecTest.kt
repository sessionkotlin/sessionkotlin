package dsl.enabled

import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.RoleNotEnabledException
import com.github.d_costa.sessionkotlin.dsl.exception.UnfinishedRolesException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
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
                    branch("Case1") {
                        x()
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
        val aux: GlobalProtocol = {
            send<Int>(b, c)
        }

        globalProtocolInternal {
            choice(b) {
                branch("Case1") {
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
                    branch("1") {
                        // b not enabled
                        branch1()
                    }
                    branch("2") {
                        branch2()
                    }
                }
            }
        }
    }
}
