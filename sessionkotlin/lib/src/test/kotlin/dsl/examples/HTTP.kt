package dsl.examples

import org.david.sessionkotlin_lib.dsl.SKRole
import org.david.sessionkotlin_lib.dsl.globalProtocolInternal
import org.david.sessionkotlin_lib.dsl.types.LEnd
import org.david.sessionkotlin_lib.dsl.types.LocalTypeReceive
import org.david.sessionkotlin_lib.dsl.types.LocalTypeSend
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
