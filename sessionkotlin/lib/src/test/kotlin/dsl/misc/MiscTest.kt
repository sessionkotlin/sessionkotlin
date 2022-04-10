package dsl.misc

import org.david.sessionkotlin_lib.dsl.SKRole
import org.david.sessionkotlin_lib.dsl.exception.ProjectionTargetException
import org.david.sessionkotlin_lib.dsl.globalProtocolInternal
import org.david.sessionkotlin_lib.dsl.types.asFormattedString
import org.david.sessionkotlin_lib.dsl.types.asString
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class MiscTest {
    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
    }

    @Test
    fun `projecting unused`() {
        val g = globalProtocolInternal {
            send<Unit>(a, b)
        }
        assertFailsWith<ProjectionTargetException> {
            g.project(c)
        }
    }

    @Test
    fun `test dump`() {
        val case1 = globalProtocolInternal {
            send<Int>(b, c)
            send<Int>(c, a)
        }

        globalProtocolInternal {
            choice(b) {
                case("1") {
                    exec(case1)
                    send<Int>(c, a)
                }
            }
        }.dump()
    }

    @Test
    fun `test toStrings`() {
        val case1 = globalProtocolInternal {
            send<Int>(b, c)
            send<Int>(c, a)
        }

        val g = globalProtocolInternal {
            choice(b) {
                case("1") {
                    exec(case1)
                    send<Int>(c, a)
                }
            }
        }
        g.project(a).asFormattedString()
        g.project(a).asString()
    }
}
