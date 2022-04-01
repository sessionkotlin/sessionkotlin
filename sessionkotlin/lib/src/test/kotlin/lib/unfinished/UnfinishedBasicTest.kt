package lib.unfinished

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.UnfinishedRolesException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class UnfinishedBasicTest {
    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
        val d = Role("D")
    }

    @Test
    fun `unfinished roles`() {
        assertFailsWith<UnfinishedRolesException> {
            globalProtocol {
                choice(a) {
                    case("Case1") {
                        send<Unit>(a, b)
                        // 'c' not enabled
                        // 'b' enabled
                    }
                    case("Case2") {
                        send<Unit>(a, c)
                        // 'c' enabled
                        // 'b' not enabled
                    }
                }
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
                // 'c' not enabled in any branch
            }
        }
    }
}
