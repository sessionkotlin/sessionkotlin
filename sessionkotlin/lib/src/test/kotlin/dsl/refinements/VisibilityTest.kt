package dsl.refinements

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.UnknownMessageLabelException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class VisibilityTest {

    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
        val d = SKRole("D")
    }

    @Test
    fun `basic example`() {
        globalProtocolInternal {
            send<Int>(a, b, "val1")
            send<Int>(b, a, "val2", "val2 > val1")
        }
    }

    @Test
    fun `simple choice`() {
        globalProtocolInternal {
            send<Int>(b, a, "init")
            choice(a) {
                branch("1") {
                    send<Int>(a, b, "val1", "val1 > init")
                }
                branch("2") {
                    send<Int>(a, b, "val2", "val2 <= init")
                }
            }
        }
    }

    @Test
    fun `label does not exist`() {
        assertFailsWith<UnknownMessageLabelException> {
            globalProtocolInternal {
                send<Int>(a, b, "val1", "val1 > init")
            }
        }
    }

    @Test
    fun `label does not exist yet`() {
        assertFailsWith<UnknownMessageLabelException> {
            globalProtocolInternal {
                send<Int>(a, b, "val1", "val1 > val2")
                send<Int>(a, b, "val2")
            }
        }
    }

    @Test
    fun `label does not exist in the branch`() {
        assertFailsWith<UnknownMessageLabelException> {
            globalProtocolInternal {
                choice(a) {
                    branch("1") {
                        send<Int>(a, b, "val1")
                    }
                    branch("2") {
                        send<Int>(a, b, condition = "val1 > 0")
                    }
                }
            }
        }
    }

    @Test
    fun `label defined in exec`() {
        val aux = globalProtocolInternal {
            send<Int>(a, b, "val1")
        }
        globalProtocolInternal {
            exec(aux)
            send<Int>(a, b, "val2", "val1 < val2")
        }
    }

    @Test
    fun `locally unknown`() {
        assertFailsWith<UnknownMessageLabelException> {
            globalProtocolInternal {
                send<Int>(a, b, "val1")
                send<Int>(c, a, "val2", "val2 < val1")
            }
        }
    }

    @Test
    fun `test greater eq`() {
        assertFailsWith<UnknownMessageLabelException> {
            globalProtocolInternal {
                choice(a) {
                    branch("1") {
                        send<Int>(a, b, "val1")
                    }
                    branch("2") {
                        send<Int>(a, b, condition = "val1 >= 0")
                    }
                }
            }
        }
    }
}
