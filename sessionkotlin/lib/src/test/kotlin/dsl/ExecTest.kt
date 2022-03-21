package dsl

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.InconsistentExternalChoiceException
import org.david.sessionkotlin_lib.dsl.exception.RecursiveProtocolException
import org.david.sessionkotlin_lib.dsl.exception.RoleNotEnabledException
import org.david.sessionkotlin_lib.dsl.Samples
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class ExecTest {

    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
    }

    @Test
    fun `normal exec`() {

        val x = globalProtocol {
            send<Int>(b, c)
        }

        globalProtocol {
            send<Int>(a, b)
            exec(x)
        }
    }

    @Test
    fun `normal exec 2`() {

        val x = globalProtocol {
            send<Int>(c, a)
        }

        globalProtocol {
            send<Int>(a, b)
            exec(x)
        }
    }

    @Test
    fun `exec not enabled`() {
        val x = globalProtocol {
            send<Int>(c, b)
        }
        assertFailsWith<RoleNotEnabledException> {
            globalProtocol {
                send<Int>(a, b)
                choice(b) {
                    case("Case1") {
                        exec(x)
                    }
                }
            }
        }
    }

    @Test
    fun `inconsistent external in exec`() {
        val case1 = globalProtocol {
            send<Int>(b, c)
            send<Int>(c, a)
        }

        val case2 = globalProtocol {
            send<String>(b, a)
        }

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {

                    case("Case 1") {
                        exec(case1)
                    }
                    case("Case 2") {
                        exec(case2)
                    }
                }
            }
        }
    }

    @Test
    fun `activated in exec`() {

        val case1 = globalProtocol {
            send<Int>(b, c)
            send<Int>(c, a)
        }

        globalProtocol {
            choice(b) {
                case("Case 1") {
                    exec(case1)
                    send<Int>(c, a)
                }
            }
        }
    }

    @Test
    fun `rec inside exec`() {

        val case1 = globalProtocol {
            send<Int>(b, c)
            rec()
        }

        assertFailsWith<RecursiveProtocolException> {
            globalProtocol {
                choice(b) {

                    case("Case 1") {
                        exec(case1)
                        send<Int>(b, c)
                    }
                }
            }
        }
    }

    @Test
    fun `test exec example`() {
        Samples().exec()
    }

    @Test
    fun `exec dump`() {

        val case1 = globalProtocol {
            send<Int>(b, c)
            send<Int>(c, a)
        }

        globalProtocol {
            choice(b) {
                case("Case 1") {
                    exec(case1)
                    send<Int>(c, a)
                }
            }
        }.dump()
    }

    @Test
    fun `exec after rec`() {
        val aux = globalProtocol {

        }
        assertFailsWith<RecursiveProtocolException> {
            globalProtocol {
                send<Int>(a, b)
                rec()
                exec(aux)

            }
        }
    }
}