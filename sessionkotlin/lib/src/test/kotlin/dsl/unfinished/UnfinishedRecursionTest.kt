package dsl.unfinished

import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.exception.UnfinishedRolesException
import org.david.sessionkotlin.dsl.globalProtocolInternal
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
                val t = miu()
                choice(a) {
                    branch("1") {
                        send<Unit>(a, c)
                        // 'b' is not enabled
                        goto(t)
                    }
                    branch("2") {
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
                    branch("1") {
                        send<Int>(a, b)
                        val t = miu()
                        send<Int>(b, c)

                        choice(c) {
                            branch("1.1") {
                                send<String>(c, a)
                                // 'd' not enabled
                                goto(t)
                            }
                            branch("1.2") {
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
                val x = miu()
                send<Int>(a, b)
                choice(a) {
                    branch("1") {
                        goto(x)
                    }
                    branch("2") {
                        val y = miu()
                        send<Long>(a, b)
                        choice(a) {
                            branch("1.1") {
                                // 'b' not enabled
                                goto(y)
                            }
                            branch("1.2") {
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
                val x = miu()
                choice(a) {
                    branch("1") {
                        // 'b' not used before goto
                        // 'b' not enabled
                        goto(x)
                    }
                    branch("2") {
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
            val x = miu()
            choice(a) {
                branch("1") {
                    send<Long>(a, b)
                    goto(x)
                }
                branch("2") {
                    send<Int>(a, b)
                }
            }
        }
    }
}
