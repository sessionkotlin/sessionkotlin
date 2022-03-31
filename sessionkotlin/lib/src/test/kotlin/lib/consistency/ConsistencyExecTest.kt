package lib.consistency

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.InconsistentExternalChoiceException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class ConsistencyExecTest {
    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
    }

    @Test
    fun `inconsistent external in exec`() {
        val case1 = globalProtocol {
            send<Int>(b, c)
            send<Int>(c, a)
            // 'a' enabled by 'c'
        }

        val case2 = globalProtocol {
            send<Int>(b, c)
            send<String>(b, a)
            // 'a' enabled by 'b'
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
    fun `consistent external in exec after map`() {
        val case1 = globalProtocol {
            send<Int>(b, c)
            send<Int>(c, a) // 'a' will be replaced by 'b'
        }
        val case2 = globalProtocol {
            send<Int>(b, c)
        }
        globalProtocol {
            choice(b) {
                case("1") {
                    exec(case1, mapOf(a to b))
                }
                case("2") {
                    exec(case2)
                }
            }
        }
    }


}