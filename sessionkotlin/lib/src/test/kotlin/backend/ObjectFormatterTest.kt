package backend

import com.github.sessionkotlin.lib.backend.message.ObjectFormatter
import com.github.sessionkotlin.lib.backend.message.SKMessage
import org.junit.jupiter.api.Test
import java.nio.ByteBuffer
import kotlin.test.assertEquals

class ObjectFormatterTest {

    @Test
    fun `test one msg`() {
        val f = ObjectFormatter()
        val data = SKMessage("label", 10)

        val bytes = ByteBuffer.wrap(f.toBytes(data))

        val processed = f.fromBytes(bytes)
        assert(processed.isPresent)
        assertEquals(data, (processed.get()))
    }
}
