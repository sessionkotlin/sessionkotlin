package dsl.refinements

import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.exception.InvalidRefinementValueException
import com.github.d_costa.sessionkotlin.dsl.exception.UnknownMessageLabelException
import com.github.d_costa.sessionkotlin.dsl.exception.UnsatisfiableRefinementsException
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertFailsWith

class RefinementTest {

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
        val aux: GlobalProtocol = {
            send<Int>(a, b, "val1")
        }
        globalProtocolInternal {
            aux()
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

    @Test
    fun `test arithmetic`() {
        globalProtocolInternal {
            send<Int>(a, b, "val1")
            send<Int>(a, b, "val2", condition = "(-val1 + 1) > val2 + val1")
        }
    }

    @Test
    fun `test string literal equals`() {
        globalProtocolInternal {
            send<String>(a, b, "val1", "'something' == 'something'")
            send<String>(a, b, "val2", condition = "val1 == 'something'")
        }
    }

    @Test
    fun `test string literal not equals`() {
        globalProtocolInternal {
            send<String>(a, b, "val1")
            send<String>(a, b, "val2", condition = "val2 != 'something'")
            send<String>(a, b, "val3", condition = "'else' != val3")
        }
    }

    @Test
    fun `test string literal not equals 2`() {
        globalProtocolInternal {
            send<String>(a, b, "val1")
            send<String>(a, b, "val2", condition = "val1 != val2")
        }
    }

    @Test
    fun `test empty string literal`() {
        globalProtocolInternal {
            send<String>(a, b, "val1", "val1 != ''")
        }
    }

    @Test
    fun `test invalid refinement value type`() {
        assertFailsWith<InvalidRefinementValueException> {
            globalProtocolInternal {
                send<LocalDate>(a, b, "val1", "val1 > 0")
            }
        }
    }

    @Test
    fun `test invalid refinement value type but not used`() {
        globalProtocolInternal {
            send<LocalDate>(a, b, "val1")
        }
    }

    @Test
    fun `test unsat same message`() {
        assertFailsWith<UnsatisfiableRefinementsException> {
            globalProtocolInternal {
                send<Int>(a, b, "val1", "val1 < 0 && val1 > 0")
            }
        }
    }

    @Test
    fun `test unsat sifferent messages`() {
        assertFailsWith<UnsatisfiableRefinementsException> {
            globalProtocolInternal {
                send<Int>(a, b, "val1", "val1 < 0")
                send<Int>(a, b, "val2", "val2 > 0 && val2 == val1")
            }
        }
    }

    @Test
    fun `test unsat no branch`() {
        assertFailsWith<UnsatisfiableRefinementsException> {
            globalProtocolInternal {
                choice(a) {
                    branch("1") {
                        send<Int>(a, b, "val1")
                    }
                    branch("2") {
                        send<Int>(a, b, "val2", "0 == 1")
                    }
                }
            }
        }
    }
}
