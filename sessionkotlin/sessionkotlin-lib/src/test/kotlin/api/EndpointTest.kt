package api

import com.github.d_costa.sessionkotlin.api.SKLinearEndpoint
import com.github.d_costa.sessionkotlin.api.exception.SKLinearException
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class EndpointTest {

    @Test
    fun `test linear endpoint`() {
        val e = SKLinearEndpoint()
        e.use()
        assertFailsWith<SKLinearException> {
            e.use()
        }
    }
}
