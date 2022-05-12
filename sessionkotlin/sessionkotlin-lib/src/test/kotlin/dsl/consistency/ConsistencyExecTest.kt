package dsl.consistency

import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.InconsistentExternalChoiceException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
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
            send<Int>(b, c)
            send<Int>(c, a)
            // 'a' enabled by 'c'
        }

        val case2: GlobalProtocol = {
            send<Int>(b, c)
            send<String>(b, a)
            // 'a' enabled by 'b'
        }

        assertFailsWith<InconsistentExternalChoiceException> {
            globalProtocolInternal {
                choice(b) {
                    branch("Case1") {
                        case1()
                    }
                    branch("Case2") {
                        case2()
                    }
                }
            }
        }
    }
}
