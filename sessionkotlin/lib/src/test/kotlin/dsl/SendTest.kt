package dsl

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.exception.SendingtoSelfException
import org.david.sessionkotlin_lib.dsl.Samples
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class SendTest {

    companion object {
        val a = Role("A")
        val b = Role("B")
        val c = Role("C")
    }

    @Test
    fun `normal send`() {

        globalProtocol {
            send<Int>(a, b)
        }

    }

    @Test
    fun `same role sending and receiving`() {

        assertFailsWith<SendingtoSelfException> {
            globalProtocol {
                send<Int>(a, a)
            }
        }
    }

    @Test
    fun `three roles`() {

        globalProtocol {
            send<Int>(a, b)
            send<Int>(c, b)
            send<Int>(b, a)
        }
    }

    @Test
    fun `test send example`() {
        Samples().send()
    }
}