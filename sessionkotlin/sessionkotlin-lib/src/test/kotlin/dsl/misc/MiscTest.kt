package dsl.misc

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.ProjectionTargetException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.asFormattedString
import com.github.d_costa.sessionkotlin.dsl.types.asString
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
                branch("1") {
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
                branch("1") {
                    exec(case1)
                    send<Int>(c, a)
                }
            }
        }
        g.project(a).asFormattedString()
        g.project(b).asFormattedString()
        g.project(a).asString()
        g.project(b).asString()
    }
}
