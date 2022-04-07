package lib.enabled

import lib.util.IntClass
import lib.util.LongClass
import lib.util.StringClass
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin_lib.dsl.globalProtocolInternal
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnabledBasicTest {
    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
        val d = Role("D")
    }

    @Test
    fun `role not enabled but is ignorable`() {
        val g = globalProtocolInternal {
            send<Int>(a, b)
            send<Int>(b, a)

            choice(b) {
                case("1") {
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
    fun `role not enabled 2 cases mergeable`() {
        val g = globalProtocolInternal {
            send<Int>(a, b)
            send<Int>(b, a)

            // mergeable for 'a'
            choice(b) {
                case("Case1") {
                    send<String>(b, c)
                    send<String>(a, b)
                }
                case("Case2") {
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
    fun `role not enabled 2 cases not mergeable`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocolInternal {
                choice(b) {
                    case("Case1") {
                        send<String>(b, c)
                        // 'a' not enabled
                        send<String>(a, b)
                    }
                    case("Case2") {
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
                case("Case1") {
                    send<String>(b, a)
                    send<String>(d, a)
                }
                case("Case2") {
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
                case("Case1") {
                    send<String>(b, a)
                    send<String>(c, d)
                    send<String>(d, a)
                }
                case("Case2") {
                    send<Int>(b, a)
                    send<String>(c, d)
                    send<String>(d, a)
                }
            }
        }
        val lC = LocalTypeSend(d, StringClass, LEnd)
        assertEquals(g.project(c), lC)
    }

    @Test
    fun `role not enabled to send not mergeable`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocolInternal {
                choice(b) {
                    case("Case1") {
                        send<String>(b, a)
                    }
                    case("Case2") {
                        send<String>(b, a)
                        send<String>(c, b)
                    }
                }
            }
        }
    }

    @Test
    fun `role not enabled to choose not mergeable`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocolInternal {
                choice(b) {
                    case("Case1") {
                        choice(a) {
                            case("SubCase1") {
                                send<Int>(a, b)
                            }
                        }
                    }
                    case("Case2") {
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
                case("Case1") {
                    choice(a) {
                        case("SubCase1") {
                            send<Int>(a, b)
                        }
                    }
                }
            }
        }
        val lA = LocalTypeInternalChoice(
            mapOf("SubCase1" to LocalTypeSend(b, IntClass, LEnd))
        )
        assertEquals(g.project(a), lA)
    }

    @Test
    fun `role activated`() {
        globalProtocolInternal {
            choice(b) {
                case("Case1") {
                    send<String>(b, a)
                }
                case("Case2") {
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
                case("Case1") {
                    send<String>(b, c)
                    send<String>(c, a)
                }
                case("Case2") {
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
                case("1") {
                    send<Long>(a, b)
                    send<Int>(b, c)
                    send<String>(a, b)
                }
                case("2") {
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
                case("1") {
                    send<Long>(a, b)

                    // b does not participate, and can ignore the choice
                    choice(a) {
                        case("1.1") {
                            send<Int>(a, c)
                        }
                        case("1.2") {
                            send<String>(a, c)
                        }
                    }
                }
                case("2") {
                    send<Int>(a, b)
                    send<Boolean>(a, c)
                }
            }
        }
        val lB = LocalTypeExternalChoice(
            a,
            mapOf(
                "1" to LocalTypeReceive(a, LongClass, LEnd),
                "2" to LocalTypeReceive(a, IntClass, LEnd)
            )
        )
        assertEquals(g.project(b), lB)
    }
}
