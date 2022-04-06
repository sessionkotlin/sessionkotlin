package lib.examples

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.globalProtocolInternal
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ProxyExample {

    @Test
    fun main() {
        val client = Role("Client")
        val proxy = Role("Proxy")
        val server = Role("Server")

        val g = globalProtocolInternal {
            send<Request>(client, proxy)

            send<Request>(proxy, server)

            choice(server) {
                case("Ok") {
                    send<Response>(server, proxy)
                    send<Response>(proxy, client)
                }
                case("Error") {
                    send<Error>(server, proxy)
                    send<Error>(proxy, client)
                }
            }
        }

        val lClient = LocalTypeSend(
            proxy, Request::class.java,
            LocalTypeExternalChoice(
                proxy,
                mapOf(
                    "Ok" to LocalTypeReceive(proxy, Response::class.java, LEnd),
                    "Error" to LocalTypeReceive(proxy, Error::class.java, LEnd)
                )
            )
        )
        val lProxy = LocalTypeReceive(
            client, Request::class.java,
            LocalTypeSend(
                server, Request::class.java,
                LocalTypeExternalChoice(
                    server,
                    mapOf(
                        "Ok" to LocalTypeReceive(
                            server,
                            Response::class.java,
                            LocalTypeSend(client, Response::class.java, LEnd)
                        ),
                        "Error" to LocalTypeReceive(
                            server,
                            Error::class.java,
                            LocalTypeSend(client, Error::class.java, LEnd)
                        )
                    )
                )
            )
        )
        val lServer = LocalTypeReceive(
            proxy, Request::class.java,
            LocalTypeInternalChoice(
                mapOf(
                    "Ok" to LocalTypeSend(proxy, Response::class.java, LEnd),
                    "Error" to LocalTypeSend(proxy, Error::class.java, LEnd)
                )
            )
        )

        assertEquals(g.project(client), lClient)
        assertEquals(g.project(proxy), lProxy)
        assertEquals(g.project(server), lServer)
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
