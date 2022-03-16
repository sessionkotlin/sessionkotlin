import org.junit.jupiter.api.Test
import sessionkotlin.dsl.Role
import sessionkotlin.dsl.Samples
import sessionkotlin.dsl.exception.InconsistentExternalChoiceException
import sessionkotlin.dsl.exception.RoleNotEnabledException
import sessionkotlin.dsl.exception.UnfinishedRolesException
import sessionkotlin.dsl.globalProtocol
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
    fun `role not enabled to send`() {

        assertFailsWith<RoleNotEnabledException> {
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
    }

    @Test
    fun `role not enabled to send 2`() {

        assertFailsWith<RoleNotEnabledException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        send<String>(b, a)
                    }
                    case("Case2") {
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

        assertFailsWith<RoleNotEnabledException> {
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
    fun `role activated 3`() {

        globalProtocol {
            choice(b) {
                case("Case1") {
                    choice(b) {
                        case("SubCase1") {
                            send<String>(b, c)
                        }
                        case("SubCase2") {
                            send<Int>(b, c)
                        }
                    }
                    send<Int>(c, b)
                }
            }

        }
    }
}