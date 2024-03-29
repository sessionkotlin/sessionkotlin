package api

import com.github.sessionkotlin.lib.dsl.GlobalProtocol
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.globalProtocol
import org.junit.jupiter.api.Test

class APIGenTest {
    companion object {
        val A = SKRole("A")
        val B = SKRole("B")
    }

    @Test
    fun `test api generation`() {
        globalProtocol("Test", callbacks = false) {
            send<Int>(A, B)
            choice(A) {
                branch {
                    send<Long>(A, B, "val1", "val1 > 0")
                }
                branch {
                    send<Long>(A, B, "val2", "val2 <= 0")
                }
            }
        }
    }

    @Test
    fun `test api generation and callbacks`() {
        globalProtocol("Test", callbacks = true) {
            send<Int>(A, B, "val0")
            choice(A) {
                branch {
                    send<Long>(A, B, "val1", "val1 > 0")
                }
                branch {
                    send<Long>(A, B, "val2", "val2 <= 0")
                }
            }
        }
    }

    @Test
    fun `test api generation with recursion`() {
        globalProtocol("Test", callbacks = false) {
            send<Int>(A, B)
            val t = mu()
            choice(A) {
                branch {
                    send<Long>(A, B, "val1", "val1 > 0")
                }
                branch {
                    send<Long>(A, B, "val2", "val2 <= 0")
                    goto(t)
                }
            }
        }
    }

    @Test
    fun `test api generation with recursion and callbacks`() {
        globalProtocol("Test", callbacks = true) {
            send<Int>(A, B, "val0")
            val t = mu()
            choice(A) {
                branch {
                    send<Long>(A, B, "val1", "val1 > 0")
                }
                branch {
                    send<Long>(A, B, "val2", "val2 <= 0")
                    goto(t)
                }
            }
        }
    }

    @Test
    fun `test fluent does not require labels`() {
        globalProtocol("Test", callbacks = false) {
            send<Int>(A, B)
        }
    }

    @Test
    fun `test sub protocols`() {
        val p1: GlobalProtocol = {
            send<Long>(A, B, "val1", "val1 > 0")
        }

        val p2: GlobalProtocol = {
            send<Long>(A, B, "val2", "val2 <= 0")
        }

        globalProtocol("Test", callbacks = true) {
            send<Int>(A, B, "val0")
            val t = mu()

            choice(A) {
                branch {
                    p1()
                }
                branch {
                    p2()
                    goto(t)
                }
            }
        }
    }
}
