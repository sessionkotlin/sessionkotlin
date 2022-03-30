package lib

import org.david.sessionkotlin_lib.dsl.RecursionTag
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.Samples
import org.david.sessionkotlin_lib.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin_lib.dsl.exception.TerminalInstructionException
import org.david.sessionkotlin_lib.dsl.exception.UndefinedRecursionVariableException
import org.david.sessionkotlin_lib.dsl.exception.UnfinishedRolesException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class RecTest {

    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
        val d = Role("D")
    }

    @Test
    fun `normal rec`() {
        globalProtocol {
            val t = miu("X")
            send<Int>(a, b)
            send<Int>(b, a)
            goto(t)
        }
    }

    @Test
    fun `illegal rec`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
                val t = miu("X")
                send<Int>(a, b)
                send<Int>(b, a)
                goto(t)
                send<Int>(b, c)
            }
        }
    }

    @Test
    fun `illegal rec reversed`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
                val t = miu("X")
                send<Int>(a, b)
                send<Int>(b, a)
                goto(t)
                send<Int>(c, b)
            }
        }
    }

    @Test
    fun `illegal rec choice at`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
                val t = miu("X")
                send<Int>(a, b)
                send<Int>(b, a)
                goto(t)
                choice(b) {}
            }
        }
    }


    @Test
    fun `illegal rec choice at 2`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
                val t = miu("X")
                choice(b) {
                    case("1") {
                        goto(t)
                        send<Int>(b, a)
                    }
                }
            }
        }
    }

    @Test
    fun `illegal rec choice case`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
                val t = miu("X")
                send<Int>(a, b)
                send<Int>(b, a)
                goto(t)
                choice(c) {
                    case("Case 1") {
                        send<Int>(c, b)
                    }
                }
            }
        }
    }

    @Test
    fun `illegal rec choice case 2`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
                val t = miu("X")
                send<Int>(a, b)
                goto(t)
                choice(c) {
                    case("Case 1") {
                        send<Int>(c, b)
                    }
                }
            }
        }
    }


    @Test
    fun `test rec example`() {
        Samples().goto()
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
    fun `undefined rec variable`() {
        assertFailsWith<UndefinedRecursionVariableException> {
            globalProtocol {
                send<Int>(a, b)
                send<Int>(c, b)
                choice(b) {
                    case("1") {
                        send<Int>(b, c)
                        goto(RecursionTag("X"))
                    }
                    case("2") {
                        send<Int>(b, c)
                    }
                }
            }
        }
    }

    @Test
    fun `undefined rec variable 2`() {
        assertFailsWith<UndefinedRecursionVariableException> {
            lateinit var t: RecursionTag
            globalProtocol {
                t = miu("X")
            }
            globalProtocol {
                send<Int>(a, b)
                send<Int>(c, b)
                choice(b) {
                    case("1") {
                        send<Int>(b, c)
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
    fun `empty loop`() {
        globalProtocol {
            send<Int>(a, b)
            send<Int>(c, b)
            val t = miu("X")
            goto(t)
        }
    }

    @Test
    fun `rec def after goto`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
                val t1 = miu("X")
                send<Unit>(a, b)
                goto(t1)

                val t2 = miu("Y")
                send<Unit>(b, a)
                goto(t2)
            }
        }
    }

    @Test
    fun `erasable choice goto`() {
        globalProtocol {
            val t = miu("X")
            send<Unit>(c, d)

            choice(a) {
                case("1") {
                    send<Unit>(a, b)
                    goto(t)
                }
            }
        }
    }

    @Test
    fun `unfinished role`() {
        assertFailsWith<UnfinishedRolesException> {
            globalProtocol {
                val t = miu("X")
                choice(a) {
                    case("1") {
                        send<Unit>(a, c)
                        goto(t)
                    }
                    case("2") {
                        send<Unit>(a, b)
                        send<Unit>(a, c)
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
    fun `rec and choice not enabled but mergeable`() {
        globalProtocol {
            val t = miu("X")
            choice(a) {
                case("1") {
                    send<Unit>(a, b)
                    choice(b) {
                        // C not enabled
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
    fun `rec and choice  unfinished`() {
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
                                goto(t)  // unfinished D
                            }
                            case("1.2") {
                                send<Int>(c, a)
                                send<String>(c, d)
                                goto(t)
                            }

                        }
                    }
                }
            }
        }

    }


    @Test
    fun `double rec not enabled`() {
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
                        goto(y)
                    }
                    case("3") {
                        send<Long>(a, b)
                    }
                }

            }
        }
    }

    @Test
    fun `ignored empty rec`() {
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


}