package backend

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.david.sessionkotlin.api.SKGenRole
import org.david.sessionkotlin.backend.SKMPEndpoint
import org.david.sessionkotlin.backend.channel.SKChannel
import org.david.sessionkotlin.backend.exception.BinaryEndpointsException
import org.david.sessionkotlin.backend.exception.ReadClosedChannelException
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class SKChannelTest {
    companion object {
        object A : SKGenRole()
        object B : SKGenRole()
        object C : SKGenRole()
        object D : SKGenRole()
    }

    @Test
    fun `test channel`() {
        val chan1 = SKChannel()
        chan1.getEndpoints(A)
        chan1.getEndpoints(A)
        chan1.getEndpoints(B)
        chan1.getEndpoints(B)
        chan1.getEndpoints(A)

        val chan2 = SKChannel()
        chan2.getEndpoints(A)
        chan2.getEndpoints(B)
        chan2.getEndpoints(A)
        chan2.getEndpoints(B)
        chan2.getEndpoints(A)
        chan2.getEndpoints(B)
    }

    @Test
    fun `test channel 1`() {
        val chan1 = SKChannel(A)
        chan1.getEndpoints(A)
        chan1.getEndpoints(A)
        chan1.getEndpoints(B)
        chan1.getEndpoints(B)
        chan1.getEndpoints(A)

        val chan2 = SKChannel(A)
        chan2.getEndpoints(A)
        chan2.getEndpoints(B)
        chan2.getEndpoints(A)
        chan2.getEndpoints(B)
        chan2.getEndpoints(A)
        chan2.getEndpoints(B)

        val chan3 = SKChannel(A)
        chan3.getEndpoints(A)
        chan3.getEndpoints(A)
        chan3.getEndpoints(B)
        chan3.getEndpoints(B)
        chan3.getEndpoints(A)

        val chan4 = SKChannel(B)
        chan4.getEndpoints(A)
        chan4.getEndpoints(B)
        chan4.getEndpoints(A)
        chan4.getEndpoints(B)
        chan4.getEndpoints(A)
        chan4.getEndpoints(B)
    }

    @Test
    fun `test channel 2`() {
        val chan1 = SKChannel(A, B)
        chan1.getEndpoints(A)
        chan1.getEndpoints(A)
        chan1.getEndpoints(B)
        chan1.getEndpoints(B)
        chan1.getEndpoints(A)

        val chan2 = SKChannel(A, B)
        chan2.getEndpoints(A)
        chan2.getEndpoints(B)
        chan2.getEndpoints(A)
        chan2.getEndpoints(B)
        chan2.getEndpoints(A)
        chan2.getEndpoints(B)

        val chan3 = SKChannel(A, B)
        chan3.getEndpoints(A)
        chan3.getEndpoints(A)
        chan3.getEndpoints(B)
        chan3.getEndpoints(B)
        chan3.getEndpoints(A)

        val chan4 = SKChannel(A, B)
        chan4.getEndpoints(A)
        chan4.getEndpoints(B)
        chan4.getEndpoints(A)
        chan4.getEndpoints(B)
        chan4.getEndpoints(A)
        chan4.getEndpoints(B)
    }

    @Test
    fun `test channel 2 reversed`() {
        val chan1 = SKChannel(B, A)
        chan1.getEndpoints(A)
        chan1.getEndpoints(A)
        chan1.getEndpoints(B)
        chan1.getEndpoints(B)
        chan1.getEndpoints(A)

        val chan2 = SKChannel(B, A)
        chan2.getEndpoints(A)
        chan2.getEndpoints(B)
        chan2.getEndpoints(A)
        chan2.getEndpoints(B)
        chan2.getEndpoints(A)
        chan2.getEndpoints(B)

        val chan3 = SKChannel(B, A)
        chan3.getEndpoints(A)
        chan3.getEndpoints(A)
        chan3.getEndpoints(B)
        chan3.getEndpoints(B)
        chan3.getEndpoints(A)

        val chan4 = SKChannel(B, A)
        chan4.getEndpoints(A)
        chan4.getEndpoints(B)
        chan4.getEndpoints(A)
        chan4.getEndpoints(B)
        chan4.getEndpoints(A)
        chan4.getEndpoints(B)
    }

    @Test
    fun `test channel not found`() {
        val chan1 = SKChannel()
        chan1.getEndpoints(A)
        chan1.getEndpoints(B)

        assertFailsWith<BinaryEndpointsException> {
            chan1.getEndpoints(C)
        }
    }

    @Test
    fun `test read from closed channel`() {
        val chan = SKChannel()
        assertFailsWith<ReadClosedChannelException> {
            runBlocking {
                launch {
                    SKMPEndpoint().use {
                        it.connect(B, chan)
                        it.close()
                    }
                }
                launch {
                    SKMPEndpoint().use {
                        it.connect(A, chan)
                        it.receive(A)
                    }
                }
            }
        }
    }
}
