package lib.enabled

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
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
        globalProtocol {
            send<Int>(a, b)
            send<Int>(b, a)

            // 'c' does not care
            choice(b) {
                case("1") {
                    send<String>(a, b)
                }
            }
        }
    }

    @Test
    fun `role not enabled 2 cases mergeable`() {
        globalProtocol {
            send<Int>(a, b)
            send<Int>(b, a)

            // mergeable for 'c'
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
    }

    @Test
    fun `role not enabled 2 cases not mergeable`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocol {
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
        globalProtocol {
            choice(b) {
                case("Case1") {
                    send<String>(b, a)
                    send<String>(a, d)
                    send<String>(d, a)
                }
                case("Case2") {
                    send<Int>(b, a)
                    send<String>(a, d)
                    send<String>(d, a)
                }
            }
        }
    }

    @Test
    fun `role not enabled 4 roles mergeable`() {
        globalProtocol {
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
    }

    @Test
    fun `role not enabled to send not mergeable`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocol {
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
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        choice(a) {
                            case("SubCase1") {
                                send<Int>(a, b)
                            }
                        }
                    }
                    case("Case 2") {
                        send<Int>(b, a)
                    }
                }
            }
        }
    }

    @Test
    fun `internal choice while ignoring external choice`() {
        globalProtocol {
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
    }

    @Test
    fun `role activated`() {
        globalProtocol {
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
        globalProtocol {
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
        globalProtocol {
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
        globalProtocol {
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
    }
}
