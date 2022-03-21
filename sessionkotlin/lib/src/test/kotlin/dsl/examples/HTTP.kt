package examples

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
import java.util.*

class HTTP {

    @Test
    fun main() {
        val c = Role("Client")
        val s = Role("Server")

        globalProtocol {
            send<HttpRequest>(c, s)
            send<HttpResponse>(c, s)
        }
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