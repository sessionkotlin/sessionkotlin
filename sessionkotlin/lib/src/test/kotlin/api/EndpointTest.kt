package api

import com.github.sessionkotlin.lib.api.exception.SKLinearException
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class EndpointTest {

    @Test
    fun `test linear endpoint`() {
        val e = com.github.sessionkotlin.lib.api.SKLinearEndpoint()
        e.use()
        assertFailsWith<SKLinearException> {
            e.use()
        }
    }
}
