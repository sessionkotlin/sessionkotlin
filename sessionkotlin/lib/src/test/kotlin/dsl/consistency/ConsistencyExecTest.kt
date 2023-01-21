package dsl.consistency

import com.github.sessionkotlin.lib.dsl.GlobalProtocol
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.exception.InconsistentExternalChoiceException
import com.github.sessionkotlin.lib.dsl.globalProtocolInternal
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class ConsistencyExecTest {
    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
    }

    @Test
    fun `inconsistent external in exec`() {
        val case1: GlobalProtocol = {
            send<Int>(b, c, "c1")
            send<Int>(c, a, "c1")
            // 'a' enabled by 'c'
        }

        val case2: GlobalProtocol = {
            send<Int>(b, c, "c2")
            send<String>(b, a, "c2")
            // 'a' enabled by 'b'
        }

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocolInternal {
                choice(b) {
                    branch {
                        case1()
                    }
                    branch {
                        case2()
                    }
                }
            }
        }
    }
}
