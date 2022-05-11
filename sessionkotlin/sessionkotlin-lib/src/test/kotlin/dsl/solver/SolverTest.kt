package dsl.solver

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import org.junit.jupiter.api.Test

class SolverTest {

    companion object {
        val a = SKRole("A")
        val b = SKRole("B")
        val c = SKRole("C")
        val d = SKRole("D")
    }

    @Test
    fun `no refinements and no variables`() {
        globalProtocolInternal {
            send<Int>(a, b)
        }
    }

    @Test
    fun `no refinements`() {
        globalProtocolInternal {
            send<Int>(a, b, "something")
        }
    }

    @Test
    fun `no refinements in the same message`() {
        globalProtocolInternal {
            send<Int>(a, b, "something")
            send<Int>(b, a, condition = "something > 10")
        }
    }
}
