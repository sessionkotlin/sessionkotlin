package dsl.unfinished

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.UnfinishedRolesException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class UnfinishedRecursionTest {
    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
        val d = SKRole("D")
    }

    @Test
    fun `unfinished role`() {
        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                val t = mu()
                choice(a) {
                    branch {
                        send<Unit>(a, c)
                        // 'b' is not enabled
                        goto(t)
                    }
                    branch {
                        send<Unit>(a, b)
                        send<Unit>(a, c)
                    }
                    // 'b' is unfinished
                }
            }
        }
    }

    @Test
    fun `rec and choice unfinished`() {
        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                choice(a) {
                    branch {
                        send<Int>(a, b)
                        val t = mu()
                        send<Int>(b, c)

                        choice(c) {
                            branch {
                                send<String>(c, a)
                                // 'd' not enabled
                                goto(t)
                            }
                            branch {
                                send<Int>(c, a)
                                send<String>(c, d)
                                // 'd' enabled
                                goto(t)
                            }
                            // 'd' unfinished
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `rec and choice unfinished 2`() {
        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                val x = mu()
                send<Int>(a, b)
                choice(a) {
                    branch {
                        goto(x)
                    }
                    branch {
                        val y = mu()
                        send<Long>(a, b)
                        choice(a) {
                            branch {
                                // 'b' not enabled
                                goto(y)
                            }
                            branch {
                                send<Long>(a, b)
                            }
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `rec and choice unfinished 3`() {
        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                val x = mu()
                choice(a) {
                    branch {
                        // 'b' not used before goto
                        // 'b' not enabled
                        goto(x)
                    }
                    branch {
                        send<Int>(a, b)
                        // 'b' enabled
                    }
                }
            }
        }
    }

    @Test
    fun `rec and choice unfinished 4`() {
        globalProtocolInternal {
            val x = mu()
            choice(a) {
                branch{
                    send<Long>(a, b)
                    goto(x)
                }
                branch {
                    send<Int>(a, b)
                }
            }
        }
    }
}
