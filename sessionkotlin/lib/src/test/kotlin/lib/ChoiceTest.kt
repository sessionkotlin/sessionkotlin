package lib

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.Samples
import org.david.sessionkotlin_lib.dsl.exception.*
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.david.sessionkotlin_lib.dsl.types.asString
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class ChoiceTest {

    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
    }

    @Test
    fun `normal choice`() {
        globalProtocol {
            choice(b) {
                case("Case1") {
                    send<String>(b, a)
                    send<String>(b, a)
                }
                case("Case2") {
                    choice(b) {
                        case("SubCase1") {
                            send<Long>(b, a)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `role not enabled but is ignorable`() {
        globalProtocol {
            send<Int>(a, b)
            send<Int>(b, a)

            choice(b) {
                case("Case1") {
                    send<String>(a, b)
                }
            }
        }
    }


    @Test
    fun `role not enabled but is ignorable 2 cases`() {
        globalProtocol {
            send<Int>(a, b)
            send<Int>(b, a)

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
    fun `role not enabled 2 cases`() {
        assertFailsWith<RoleNotEnabledException> {
            val g = globalProtocol {
                choice(b) {
                    case("Case1") {
                        send<String>(b, c)
                        send<String>(a, b)
                    }
                    case("Case2") {
                        send<Int>(a, b)
                        send<Int>(b, c)
                    }
                }
            }
            println(g.project(a).asString())
        }

    }

    @Test
    fun `role not enabled but is ignorable 4 roles`() {
        val d = Role("D")

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
    fun `role not enabled to send`() {

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
    fun `role not enabled to choose`() {

        assertFailsWith<RoleNotEnabledException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        choice(a) {
                            case("SubCase1") {
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
    fun `role activated 2`() {

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
    fun `role not activated in case`() {

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        send<String>(b, a)
                        send<String>(a, c)
                    }
                    case("Case2") {
                        send<Int>(a, c)
                        send<Int>(c, a)
                    }
                }
            }
        }
    }

    @Test
    fun `role not activated in case 2`() {

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        send<Int>(b, c)
                        send<Int>(c, a)
                    }
                    case("Case2") {
                        send<Int>(b, c)
                        send<String>(b, a)
                    }
                }
            }
        }
    }


    @Test
    fun `role enabled by different roles`() {

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        send<String>(b, c)
                    }
                    case("Case2") {
                        send<String>(b, a)
                        send<String>(a, c)
                    }
                }
            }
        }
    }

    @Test
    fun `role enabled in only one case`() {

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        send<String>(b, c)
                        send<String>(b, c)
                    }
                    case("Case2") {
                        send<String>(b, a)
                        send<String>(a, c)
                    }
                }
            }
        }
    }

    @Test
    fun `two buyers`() {
        val s = Role("S")

        globalProtocol {
            send<String>(a, s)
            send<Int>(s, a)
            send<Int>(s, b)
            send<Int>(a, b)
            choice(b) {
                case("Ok") {
                    send<String>(b, s)
                    send<String>(s, b)
                }
                case("Quit") {
                    send<String>(b, s)
                }
            }
        }
    }

    @Test
    fun `two buyers inconsistent`() {
        val s = Role("S")

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                send<String>(a, s)
                send<Int>(s, a)
                send<Int>(s, b)
                send<Int>(a, b)
                choice(b) {
                    case("Ok") {
                        send<String>(b, s)
                        send<String>(s, a)
                    }
                    case("Quit") {
                        send<String>(b, a)
                    }
                }
            }
        }

    }

    @Test
    fun `test choice example`() {
        Samples().choice()
    }

    @Test
    fun `unfinished roles`() {
        assertFailsWith<UnfinishedRolesException> {
            globalProtocol {
                choice(a) {
                    case("Case1") {
                        send<Unit>(a, b)
                    }
                    case("Case2") {
                        send<Unit>(a, c)
                    }
                }
            }
        }
    }

    @Test
    fun `send after choice`() {
        assertFailsWith<TerminalInstructionException> {
            globalProtocol {
                choice(a) {
                    case("Case1") {
                        send<Unit>(a, b)
                    }
                    case("Case2") {
                        send<Unit>(a, c)
                    }
                }
                send<Unit>(a, b)
            }
        }
    }

    @Test
    fun `erasable choice`() {
        globalProtocol {
            send<Unit>(a, b)
            send<Unit>(c, b)
            choice(a) {
                case("Case1") {
                    send<String>(a, b)
                }
                case("Case2") {
                    send<Int>(a, b)
                }
            }

        }
    }

    @Test
    fun `dupe case labels`() {
        assertFailsWith<DuplicateCaseLabelException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        send<String>(b, a)
                    }
                    case("Case1") {
                        send<Int>(b, a)
                        send<Long>(a, b)
                    }
                }
            }
        }
    }

    @Test
    fun `order does not matter`() {
        globalProtocol {
            choice(b) {
                case("Case1") {
                    send<Int>(b, c)
                    send<String>(b, a)
                }
                case("Case 2") {
                    send<String>(b, a)
                    send<Int>(b, c)
                }
            }
        }
    }

    @Test
    fun `c chooses to a`() {
        globalProtocol {
            choice(b) {
                case("Case1") {
                    send<String>(b, c)
                    send<String>(c, a)
                }
                case("Case 2") {
                    send<Int>(b, c)
                    send<Int>(c, a)
                }
            }
        }
    }

    @Test
    fun `choice agnostic`() {
        val aux = globalProtocol {
            send<String>(b, c)
            send<Int>(a, b)
            send<Long>(b, c)
        }
        globalProtocol {
            choice(a) {
                case("1") {
                    exec(aux)
                }
                case("2") {
                    exec(aux)
                }
            }
        }
    }

}