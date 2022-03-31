package lib.consistency

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.InconsistentExternalChoiceException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class ConsistencyBasicTest {
    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
    }

    @Test
    fun `role not activated in one case`() {
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        // 'a' activated
                        send<String>(b, a)
                        send<String>(a, c)
                    }
                    case("Case2") {
                        // 'a' not activated
                        send<Int>(a, c)
                        send<Int>(c, a)
                    }
                }
            }
        }
    }

    @Test
    fun `role not activated in one case 2`() {
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        send<Int>(b, c)
                        // 'a' enabled by 'c'
                        send<Int>(c, a)
                    }
                    case("Case2") {
                        send<Int>(b, c)
                        // 'a' enabled by 'b'
                        send<String>(b, a)
                    }
                }
            }
        }
    }

    @Test
    fun `role not activated in one case 3`() {
        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        send<String>(b, c)
                        send<String>(b, c)
                        // 'a' not enabled
                    }
                    case("Case2") {
                        // 'a' enabled by b
                        send<String>(b, a)
                        send<String>(a, c)
                    }
                }
            }
        }
    }

    @Test
    fun `enabled by different roles`() {

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocol {
                choice(b) {
                    case("Case1") {
                        // 'c' enabled by 'b'
                        send<String>(b, c)
                    }
                    case("Case2") {
                        send<String>(b, a)
                        // 'c' enabled by 'a'
                        send<String>(a, c)
                    }
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
                        // 'a' enabled by 's'
                        send<String>(s, a)
                    }
                    case("Quit") {
                        send<String>(b, a)
                        // 'a' not enabled
                    }
                }
            }
        }
    }

    @Test
    fun `merge inlined and non inlined`() {
        globalProtocol {
            choice(b) {
                // 'a' is not enabled in any branch
                case("1") {
                    send<String>(a, b)
                    send(a, b, Boolean::class.javaObjectType)
                }
                case("2") {
                    send(a, b, String::class.java)
                    send<Boolean>(a, b)
                }
            }
        }
    }


    @Test
    fun `test enabled by`() {
        globalProtocol {
            choice(a) {
                case("1") {
                    send<Long>(a, b)
                    send<Long>(a, c)

                    // ensure this choice does not override 'a' enabling 'b'
                    choice(c) {
                        case("1.1") {
                            send<Int>(c, b)
                        }
                        case("1.2") {
                            send<String>(c, b)
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