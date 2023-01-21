package dsl.misc

import com.github.sessionkotlin.lib.dsl.GlobalProtocol
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.exception.ProjectionTargetException
import com.github.sessionkotlin.lib.dsl.globalProtocolInternal
import com.github.sessionkotlin.lib.dsl.types.asString
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
        val case1: GlobalProtocol = {
            send<Int>(b, c)
            send<Int>(c, a)
        }

        globalProtocolInternal {
            choice(b) {
                branch {
                    case1()
                    send<Int>(c, a)
                }
            }
        }.dump()
    }

    @Test
    fun `test toStrings`() {
        val case1: GlobalProtocol = {
            send<Int>(b, c)
            send<Int>(c, a)
        }

        val g = globalProtocolInternal {
            choice(b) {
                branch {
                    case1()
                    send<Int>(c, a)
                }
            }
        }
        g.project(a).asString()
        g.project(b).asString()
    }
}
