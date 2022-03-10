import sessionkotlin.dsl.globalProtocol
import org.junit.jupiter.api.Test
import sessionkotlin.dsl.Examples
import sessionkotlin.dsl.Role
import sessionkotlin.dsl.exception.InconsistentExternalChoiceException
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

        val complex = globalProtocol {
            send<Int>(a, b)
            exec(x, mapOf(c to a))
        }

        complex.dump()
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

        @Test
        fun `test exec example`() {
            Examples().exec()
        }
    }


}