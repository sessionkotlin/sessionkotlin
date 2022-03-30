package lib

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.Samples
import org.david.sessionkotlin_lib.dsl.exception.*
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
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
        assertFailsWith<UnfinishedRolesException> {
            globalProtocol {
                send<Int>(a, b)
                choice(b) {
                    case("Case1") {
                        exec(x)
                    }
                    case("Case 2") {
                        send<Int>(b, c)
                        send<Int>(c, b)
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
            send<Int>(b, c)
            send<String>(b, a)
        }

        assertFailsWith<InconsistentExternalChoiceException> {
            val g = globalProtocol {
                choice(b) {

                    case("Case 1") {
                        exec(case1)
                    }
                    case("Case 2") {
                        exec(case2)
                    }
                }
            }

            g.dump(0)
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
            send<Int>(a, b)
        }
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
                val t = miu("X")
                send<Int>(a, b)
                goto(t)
                exec(aux)
            }
        }
    }

    @Test
    fun `mapping sending to self`() {
        assertFailsWith<SendingtoSelfException> {
            val x = globalProtocol {
                send<Int>(b, c)
            }

            globalProtocol {
                send<Int>(a, b)
                exec(x, mapOf(c to b))
            }
        }
    }

    @Test
    fun `mapping sending to self inside choice`() {
        assertFailsWith<SendingtoSelfException> {
            val x = globalProtocol {
                choice(a) {
                    case("1") {
                        send<Int>(a, b)
                    }
                }
            }

            globalProtocol {
                send<Int>(a, b)
                exec(x, mapOf(a to b))
            }
        }
    }

    @Test
    fun `consistent external in exec after map`() {
        val case1 = globalProtocol {
            send<Int>(b, c)
            send<Int>(c, a)
        }

        val case2 = globalProtocol {
            send<Int>(b, c)
        }

        globalProtocol {
            choice(b) {

                case("Case 1") {
                    exec(case1, mapOf(a to b))
                }
                case("Case 2") {
                    exec(case2)
                }
            }
        }
    }

    @Test
    fun `inconsistent external in exec after map`() {
        val x = Role("X")
        val y = Role("Y")

        val subprotocol = globalProtocol {
            send<Int>(x, y)
            send<Int>(y, x)
        }

        assertFailsWith<RoleNotEnabledException> {
            globalProtocol {
                choice(a) {
                    case("1") {
                        // b not enabled
                        exec(subprotocol, mapOf(x to b, y to a))
                    }
                    case("2") {
                        exec(subprotocol, mapOf(x to a, y to b))
                    }
                }
            }
        }
    }

    @Test
    fun `unfinished roles in exec after map`() {
        val subprotocol = globalProtocol {
            send<Int>(a, b)
            send<Int>(a, c)
        }

        assertFailsWith<UnfinishedRolesException> {
            globalProtocol {
                choice(a) {
                    case("1") {
                        exec(subprotocol)
                    }
                    case("2") {
                        exec(subprotocol, mapOf(b to c))
                    }
                }
            }
        }
    }

    @Test
    fun `roles in map but not in protocol`() {
        val subprotocol = globalProtocol {
            send<Int>(a, b)
            send<Int>(a, c)
        }
        val x = Role("X")

        val g = globalProtocol {
            choice(a) {
                case("1") {
                    exec(subprotocol)
                }
                case("2") {
                    // x to c is ignored
                    exec(subprotocol, mapOf(x to c))
                }
            }
        }
        assert(!g.roles.contains(x))
    }

    @Test
    fun `not enabled in exec`() {
        val subprotocol = globalProtocol {
            send<Int>(a, b)
            send<Int>(a, c)
        }

        assertFailsWith<UnfinishedRolesException> {
            globalProtocol {
                send<Int>(a, b)
                send<Int>(a, c)
                choice(a) {
                    case("1") {
                        exec(subprotocol, mapOf(a to b, b to a))
                    }
                    case("2") {
                        exec(subprotocol)
                    }
                }
            }
        }
    }


    @Test
    fun `reversed roles`() {
        val subprotocol = globalProtocol {
            choice(a) {
                case("1") {
                    send<String>(a, b)
                    send<String>(b, a)
                }
                case("2") {
                    send<Unit>(a, b)
                }
            }

        }
        val g = globalProtocol {
            exec(subprotocol, mapOf(a to b, b to a)) // reverse roles
        }

        val lB = LocalTypeInternalChoice(
            mapOf(
                "1" to LocalTypeSend(a, String::class.java,
                    LocalTypeReceive(a, String::class.java, LocalTypeEnd)),
                "2" to LocalTypeSend(a, Unit::class.java, LocalTypeEnd)
            ))
        val lA = LocalTypeExternalChoice(b,
            mapOf(
                "1" to LocalTypeReceive(b, String::class.java,
                    LocalTypeSend(b, String::class.java, LocalTypeEnd)),
                "2" to LocalTypeReceive(b, Unit::class.java, LocalTypeEnd)
            ))
        assertEquals(g.project(a), lA)
        assertEquals(g.project(b), lB)
    }
}