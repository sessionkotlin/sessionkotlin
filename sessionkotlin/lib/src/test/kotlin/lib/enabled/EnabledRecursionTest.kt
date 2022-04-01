package lib.enabled

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class EnabledRecursionTest {
    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
        val d = Role("D")
    }

    @Test
    fun `rec disabled role`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocol {
                val t = miu("X")
                send<Int>(a, b)
                send<Int>(c, b)
                choice(b) {
                    case("1") {
                        send<Int>(b, c)
                        // 'a' not enabled
                        goto(t)
                    }
                    case("2") {
                        send<Int>(b, c)
                    }
                }
            }
        }
    }

    @Test
    fun `rec and choice not enabled`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocol {
                val t = miu("X")
                choice(a) {
                    case("1") {
                        send<Unit>(a, b)
                        choice(b) {
                            case("1.1") {
                                send<Int>(b, c)
                                goto(t)
                            }
                            case("1.2") {
                                send<String>(b, c)
                            }
                        }
                    }
                    case("2") {
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
            globalProtocol {
                val t = miu("X")
                choice(a) {
                    case("1") {
                        send<Unit>(a, b)
                        choice(b) {
                            case("1.1") {
                                send<Int>(b, c)
                                // 'a' not enabled
                                goto(t)
                            }
                            case("1.2") {
                                send<String>(b, c)
                            }
                        }
                    }
                    case("2") {
                        send<Int>(a, b)
                        send<Int>(b, c)
                    }
                }
            }
        }
    }

    @Test
    fun `rec and choice not enabled but mergeable`() {
        globalProtocol {
            val t = miu("X")
            choice(a) {
                case("1") {
                    send<Unit>(a, b)
                    choice(b) {
                        // 'c' not enabled, but mergeable
                        case("1.1") {
                            send<Int>(b, c)
                            goto(t)
                        }
                        case("1.2") {
                            send<String>(b, c)
                            goto(t)
                        }
                    }
                }
                case("2") {
                    send<Int>(a, b)
                    send<Int>(b, c)
                }
            }
        }
    }

    @Test
    fun `nested rec not enabled`() {
        assertFailsWith<RoleNotEnabledException> {
            globalProtocol {
                send<Int>(a, c)
                val y = miu("Y")
                send<Unit>(a, b)
                send<Unit>(b, a)
                val x = miu("X")
                send<Int>(a, b)

                choice(a) {
                    case("1") {
                        send<Unit>(a, b)
                        goto(x)
                    }
                    case("2") {
                        // 'b' not enabled
                        goto(y)
                    }
                    case("3") {
                        send<Long>(a, b)
                    }
                }
            }
        }
    }
}
