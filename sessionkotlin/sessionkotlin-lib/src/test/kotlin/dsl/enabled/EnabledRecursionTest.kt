package dsl.enabled

import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.RoleNotEnabledException
import com.github.d_costa.sessionkotlin.dsl.exception.UnfinishedRolesException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import dsl.util.IntClass
import dsl.util.UnitClass
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnabledRecursionTest {
    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
        val d = SKRole("D")
    }

    @Test
    fun `rec disabled role`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocolInternal {
                val t = mu()
                send<Int>(a, b)
                send<Int>(c, b)
                choice(b) {
                    branch {
                        send<Int>(b, c)
                        // 'a' not enabled
                        goto(t)
                    }
                    branch {
                        send<Int>(b, c)
                    }
                }
            }
        }
    }

    @Test
    fun `rec and choice not enabled`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocolInternal {
                val t = mu()
                choice(a) {
                    branch {
                        send<Unit>(a, b)
                        choice(b) {
                            branch {
                                send<Int>(b, c)
                                goto(t)
                            }
                            branch {
                                send<String>(b, c)
                            }
                        }
                    }
                    branch {
                        send<Int>(a, b)
                        send<Int>(b, c)
                    }
                }
            }
        }
    }

    @Test
    fun `rec and choice not enabled nested choice`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocolInternal {
                val t = mu()
                choice(a) {
                    branch {
                        send<Unit>(a, b)
                        choice(b) {
                            branch {
                                send<Int>(b, c)
                                // 'a' not enabled
                                goto(t)
                            }
                            branch {
                                send<String>(b, c)
                            }
                        }
                    }
                    branch{
                        send<Int>(a, b)
                        send<Int>(b, c)
                    }
                }
            }
        }
    }

    @Test
    fun `rec and choice not enabled but mergeable`() {
        lateinit var t: RecursionTag
        val g = globalProtocolInternal {
            t = mu()
            choice(a) {
                branch {
                    send<Unit>(a, b)
                    choice(b) {
                        // 'a' not enabled, but mergeable
                        branch {
                            send<Int>(b, c)
                            goto(t)
                        }
                        branch {
                            send<String>(b, c)
                            goto(t)
                        }
                    }
                }
                branch {
                    send<Int>(a, b)
                    send<Int>(b, c)
                }
            }
        }
        val lA = LocalTypeRecursionDefinition(
            t,
            LocalTypeInternalChoice(
                listOf(
                    LocalTypeSend(b, UnitClass, LocalTypeRecursion(t)),
                    LocalTypeSend(b, IntClass, LEnd)
                )
            )
        )
        assertEquals(lA, g.project(a))
    }

    @Test
    fun `nested rec not enabled`() {
        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                send<Int>(a, c)
                val y = mu()
                send<Unit>(a, b)
                send<Unit>(b, a)
                val x = mu()
                send<Int>(a, b)

                choice(a) {
                    branch {
                        send<Unit>(a, b)
                        goto(x)
                    }
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
