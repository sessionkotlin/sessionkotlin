package examples

import org.junit.jupiter.api.Test
import sessionkotlin.dsl.Role
import sessionkotlin.dsl.globalProtocol
import java.util.*

class HTTPExample {

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