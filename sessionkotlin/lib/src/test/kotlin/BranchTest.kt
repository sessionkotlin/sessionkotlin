import sessionkotlin.dsl.globalProtocol
import org.junit.jupiter.api.Test
import sessionkotlin.dsl.Role
import sessionkotlin.dsl.RoleNotEnabledException
import kotlin.test.assertFailsWith

class BranchTest {

    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
    }

    @Test
    fun `normal branch`() {

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
                send<Int>(a, b)
                send<Int>(b, a)

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
                send<Int>(a, b)
                send<Int>(b, a)

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
                    send<String>(b, a)
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
}