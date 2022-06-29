package dsl.enabled

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.RoleNotEnabledException
import com.github.d_costa.sessionkotlin.dsl.exception.UnfinishedRolesException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import dsl.util.IntClass
import dsl.util.LongClass
import dsl.util.StringClass
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnabledBasicTest {
    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
        val d = SKRole("D")
    }

    @Test
    fun `role not enabled but is ignorable`() {
        val g = globalProtocolInternal {
            send<Int>(a, b)
            send<Int>(b, a)

            choice(b) {
                branch {
                    // a not enabled
                    send<String>(a, b)
                }
            }
        }
        val lA = LocalTypeSend(
            b, IntClass,
            LocalTypeReceive(
                b, IntClass,
                LocalTypeSend(b, StringClass, LEnd)
            )
        )
        assertEquals(g.project(a), lA)
    }

    @Test
    fun `role not enabled 2 branches mergeable`() {
        val g = globalProtocolInternal {
            send<Int>(a, b)
            send<Int>(b, a)

            // mergeable for 'a'
            choice(b) {
                branch {
                    send<String>(b, c)
                    send<String>(a, b)
                }
                branch {
                    send<String>(a, b)
                    send<String>(b, c)
                }
            }
        }
        val lA = LocalTypeSend(
            b, IntClass,
            LocalTypeReceive(
                b, IntClass,
                LocalTypeSend(b, StringClass, LEnd)
            )
        )
        assertEquals(g.project(a), lA)
    }

    @Test
    fun `role not enabled 2 branches not mergeable`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocolInternal {
                choice(b) {
                    branch {
                        send<String>(b, c)
                        // 'a' not enabled
                        send<String>(a, b)
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
    fun `role not enabled 3 roles mergeable`() {
        val g = globalProtocolInternal {
            choice(b) {
                branch {
                    send<String>(b, a)
                    send<String>(d, a)
                }
                branch {
                    send<Int>(b, a)
                    send<String>(d, a)
                }
            }
        }
        val lD = LocalTypeSend(a, StringClass, LEnd)
        assertEquals(g.project(d), lD)
    }

    @Test
    fun `role not enabled 4 roles mergeable`() {
        val g = globalProtocolInternal {
            choice(b) {
                branch {
                    send<String>(b, a)
                    send<String>(c, d)
                    send<String>(d, a)
                }
                branch {
                    send<Int>(b, a)
                    send<String>(c, d)
                    send<String>(d, a)
                }
            }
        }
        val lC = LocalTypeSend(d, StringClass, LEnd)
        assertEquals(lC, g.project(c))
    }

    @Test
    fun `role not enabled to send not mergeable`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocolInternal {
                choice(b) {
                    branch {
                        send<String>(b, a)
                    }
                    branch {
                        send<String>(b, a)
                        send<String>(c, b)
                    }
                }
            }
        }
    }

    @Test
    fun `role not enabled to choose not mergeable`() {
        assertFailsWith<UnfinishedRolesException> {
            globalProtocolInternal {
                choice(b) {
                    branch {
                        choice(a) {
                            branch {
                                send<Int>(a, b)
                            }
                        }
                    }
                    branch {
                        send<Int>(b, a)
                    }
                }
            }
        }
    }

    @Test
    fun `internal choice while ignoring external choice`() {
        val g = globalProtocolInternal {
            choice(b) {
                branch {
                    choice(a) {
                        branch {
                            send<Int>(a, b)
                        }
                    }
                }
            }
        }
        val lA = LocalTypeInternalChoice(
            listOf(LocalTypeSend(b, IntClass, LEnd))
        )
        assertEquals(lA, g.project(a))
    }

    @Test
    fun `role activated`() {
        globalProtocolInternal {
            choice(b) {
                branch {
                    send<String>(b, a)
                }
                branch {
                    send<Int>(b, a)
                    send<Long>(a, b)
                }
            }
        }
    }

    @Test
    fun `role activated transitivity`() {
        globalProtocolInternal {
            choice(b) {
                branch {
                    send<String>(b, c)
                    send<String>(c, a)
                }
                branch {
                    send<Int>(b, c)
                    send<Int>(c, a)
                    send<Int>(a, b)
                }
            }
        }
    }

    @Test
    fun `role activated transitivity 2`() {
        globalProtocolInternal {
            choice(a) {
                branch {
                    send<Long>(a, b)
                    send<Int>(b, c)
                    send<String>(a, b)
                }
                branch {
                    send<String>(a, b)
                    send<Int>(b, c)
                    send<Long>(a, b)
                }
            }
        }
    }

    @Test
    fun `erasable choice after activation`() {
        val g = globalProtocolInternal {
            choice(a) {
                branch {
                    send<Long>(a, b)

                    // b does not participate, and can ignore the choice
                    choice(a) {
                        branch {
                            send<Int>(a, c)
                        }
                        branch {
                            send<String>(a, c)
                        }
                    }
                }
                branch {
                    send<Int>(a, b)
                    send<Boolean>(a, c)
                }
            }
        }
        val lB = LocalTypeExternalChoice(
            a,
            listOf(
                LocalTypeReceive(a, LongClass, LEnd),
                LocalTypeReceive(a, IntClass, LEnd)
            )
        )
        assertEquals(g.project(b), lB)
    }
}
