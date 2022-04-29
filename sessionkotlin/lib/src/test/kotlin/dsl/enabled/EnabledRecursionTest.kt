package dsl.enabled

import dsl.util.IntClass
import dsl.util.UnitClass
import org.david.sessionkotlin.dsl.RecursionTag
import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin.dsl.globalProtocolInternal
import org.david.sessionkotlin.dsl.types.*
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
                val t = miu("X")
                send<Int>(a, b)
                send<Int>(c, b)
                choice(b) {
                    branch("1") {
                        send<Int>(b, c)
                        // 'a' not enabled
                        goto(t)
                    }
                    branch("2") {
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
                val t = miu("X")
                choice(a) {
                    branch("1") {
                        send<Unit>(a, b)
                        choice(b) {
                            branch("1.1") {
                                send<Int>(b, c)
                                goto(t)
                            }
                            branch("1.2") {
                                send<String>(b, c)
                            }
                        }
                    }
                    branch("2") {
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
                val t = miu("X")
                choice(a) {
                    branch("1") {
                        send<Unit>(a, b)
                        choice(b) {
                            branch("1.1") {
                                send<Int>(b, c)
                                // 'a' not enabled
                                goto(t)
                            }
                            branch("1.2") {
                                send<String>(b, c)
                            }
                        }
                    }
                    branch("2") {
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
            t = miu("X")
            choice(a) {
                branch("1") {
                    send<Unit>(a, b)
                    choice(b) {
                        // 'a' not enabled, but mergeable
                        branch("1.1") {
                            send<Int>(b, c)
                            goto(t)
                        }
                        branch("1.2") {
                            send<String>(b, c)
                            goto(t)
                        }
                    }
                }
                branch("2") {
                    send<Int>(a, b)
                    send<Int>(b, c)
                }
            }
        }
        val lA = LocalTypeRecursionDefinition(
            t,
            LocalTypeInternalChoice(
                mapOf(
                    "1" to LocalTypeSend(b, UnitClass, LocalTypeRecursion(t), "1"),
                    "2" to LocalTypeSend(b, IntClass, LEnd, "2")
                )
            )
        )
        assertEquals(lA, g.project(a))
    }

    @Test
    fun `nested rec not enabled`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocolInternal {
                send<Int>(a, c)
                val y = miu("Y")
                send<Unit>(a, b)
                send<Unit>(b, a)
                val x = miu("X")
                send<Int>(a, b)

                choice(a) {
                    branch("1") {
                        send<Unit>(a, b)
                        goto(x)
                    }
                    branch("2") {
                        // 'b' not enabled
                        goto(y)
                    }
                    branch("3") {
                        send<Long>(a, b)
                    }
                }
            }
        }
    }
}
