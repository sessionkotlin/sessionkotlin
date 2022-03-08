import sessionkotlin.dsl.globalProtocol
import org.junit.jupiter.api.Test
import sessionkotlin.dsl.Role
import sessionkotlin.dsl.RoleInCaseNotEnabledException
import kotlin.test.assertFailsWith

class BranchTest {
    @Test
    fun `normal branch`() {
        val a = Role("A")
        val b = Role("B")

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
        val a = Role("A")
        val b = Role("B")

        assertFailsWith<RoleInCaseNotEnabledException> {
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
    fun `role not enabled to choose`() {
        val a = Role("A")
        val b = Role("B")

        assertFailsWith<RoleInCaseNotEnabledException> {
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
        val a = Role("A")
        val b = Role("B")

        globalProtocol {
            choice(b) {
                case("Case1") {
                    send<String>(b, a)
                }
                case("Case2") {
                    send<Int>(b, a)
                    choice(a) {
                        case("SubCase1") {
                            send<Long>(a, b)
                        }
                    }
                }
            }
        }
    }
}