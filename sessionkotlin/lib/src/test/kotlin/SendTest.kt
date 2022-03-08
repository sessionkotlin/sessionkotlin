import sessionkotlin.dsl.globalProtocol
import org.junit.jupiter.api.Test
import sessionkotlin.dsl.Role
import sessionkotlin.dsl.SendingtoSelfException
import kotlin.test.assertFailsWith

class SendTest {
    @Test
    fun `normal send`() {
        val a = Role("A")
        val b = Role("B")

        globalProtocol {
            send<Int>(a, b)
        }

    }

    @Test
    fun `same role sending and receiving`() {
        val a = Role("A")

        assertFailsWith<SendingtoSelfException> {
            globalProtocol {
                send<Int>(a, a)
            }
        }
    }

    @Test
    fun `three roles`() {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")

        globalProtocol {
            send<Int>(a, b)
            send<Int>(c, b)
            send<Int>(b, a)
        }

    }
}