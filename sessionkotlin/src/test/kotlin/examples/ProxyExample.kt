package examples

import org.junit.jupiter.api.Test
import sessionkotlin.dsl.Role
import sessionkotlin.dsl.globalProtocol

class ProxyExample {

    @Test
    fun main() {
        val client = Role("Client")
        val proxy = Role("Proxy")
        val server = Role("Server")

        globalProtocol {
            send<Request>(client, proxy)

            send<Request>(proxy, server)

            choice(server) {
                case("Ok") {
                    send<Response>(server, proxy)
                }
                case("Error") {
                    send<Error>(server, proxy)
                }
            }
        }

    }

    data class Request(
        val content: String,
    )

    data class Error(
        val reason: String,
    )

    data class Response(
        val code: Int,
        val content: String,
    )
}