package backend

import com.github.d_costa.sessionkotlin.backend.message.ObjectFormatter
import com.github.d_costa.sessionkotlin.backend.message.SKPayload
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import kotlin.test.assertEquals

class ObjectFormatterTest {

    @Test
    fun `test one msg`() {
        val f = ObjectFormatter()
        val data = SKPayload(10)

        val bytes = ByteBuffer.wrap(f.toBytes(data))
        println(bytes)
        println(bytes.remaining())

        val processed = f.fromBytes(bytes)
        assert(processed.isPresent)
        assertEquals(data, (processed.get() as SKPayload<*>))
    }
}
