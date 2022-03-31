package lib.unfinished

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.UnfinishedRolesException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class UnfinishedRecursionTest {
    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
        val d = Role("D")
    }

    @Test
    fun `unfinished role`() {
        assertFailsWith<UnfinishedRolesException> {
            globalProtocol {
                val t = miu("X")
                choice(a) {
                    case("1") {
                        send<Unit>(a, c)
                        // 'b' is not enabled
                        goto(t)
                    }
                    case("2") {
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
            globalProtocol {
                choice(a) {
                    case("1") {
                        send<Int>(a, b)
                        val t = miu("X")
                        send<Int>(b, c)

                        choice(c) {
                            case("1.1") {
                                send<String>(c, a)
                                // 'd' not enabled
                                goto(t)
                            }
                            case("1.2") {
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
            globalProtocol {
                val x = miu("X")
                send<Int>(a, b)
                choice(a) {
                    case("1") {
                        goto(x)
                    }
                    case("2") {
                        val y = miu("Y")
                        send<Long>(a, b)
                        choice(a) {
                            case("1.1") {
                                // 'b' not enabled
                                goto(y)
                            }
                            case("1.2") {
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
            globalProtocol {
                val x = miu("X")
                choice(a) {
                    case("1") {
                        // 'b' not used before goto
                        // 'b' not enabled
                        goto(x)
                    }
                    case("2") {
                        send<Int>(a, b)
                        // 'b' enabled
                    }
                }
            }
        }
    }

    @Test
    fun `rec and choice unfinished 4`() {
        globalProtocol {
            val x = miu("X")
            choice(a) {
                case("1") {
                    send<Long>(a, b)
                    goto(x)
                }
                case("2") {
                    send<Int>(a, b)
                }
            }
        }
    }

}