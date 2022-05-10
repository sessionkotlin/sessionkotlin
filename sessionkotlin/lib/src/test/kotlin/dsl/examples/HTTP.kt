package dsl.examples

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.LEnd
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeReceive
import com.github.d_costa.sessionkotlin.dsl.types.LocalTypeSend
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class HTTP {

    @Test
    fun main() {
        val c = SKRole("Client")
        val s = SKRole("Server")

        val g = globalProtocolInternal {
            send<HttpRequest>(c, s)
            send<HttpResponse>(s, c)
        }
        val lC = LocalTypeSend(
            s, HttpRequest::class.java,
            LocalTypeReceive(s, HttpResponse::class.java, LEnd)
        )
        val lS = LocalTypeReceive(
            c, HttpRequest::class.java,
            LocalTypeSend(c, HttpResponse::class.java, LEnd)
        )
        assertEquals(g.project(c), lC)
        assertEquals(g.project(s), lS)
    }

    data class HttpRequest(
        val method: String,
        val host: String,
        val body: Optional<String>,
        val accept: Optional<String>,
    )

    data class HttpResponse(
        val code: Int,
        val body: String,
        val contentType: String,
    )
}
