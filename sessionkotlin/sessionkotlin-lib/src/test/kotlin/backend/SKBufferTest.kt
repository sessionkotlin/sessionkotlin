package backend

import com.github.d_costa.sessionkotlin.backend.SKBuffer
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SKBufferTest {

    @Test
    fun `test buffer value`() {
        val buf = SKBuffer<Int>()
        buf.value = 10
        assertEquals(10, buf.value)
    }

    @Test
    fun `test value not initialized`() {
        val buf = SKBuffer<Int>()
        assertFailsWith<UninitializedPropertyAccessException> {
            assertEquals(10, buf.value)
        }
    }
}
