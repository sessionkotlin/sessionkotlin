package lib.misc

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.ProjectionTargetException
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class MiscTest {
    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
    }

    @Test
    fun `projecting unused`() {
        val g = globalProtocol {
            send<Unit>(a, b)
        }
        assertFailsWith<ProjectionTargetException> {
            g.project(c)
        }
    }

    @Test
    fun `test dump`() {
        val case1 = globalProtocol {
            send<Int>(b, c)
            send<Int>(c, a)
        }

        globalProtocol {
            choice(b) {
                case("1") {
                    exec(case1)
                    send<Int>(c, a)
                }
            }
        }.dump()
    }

}