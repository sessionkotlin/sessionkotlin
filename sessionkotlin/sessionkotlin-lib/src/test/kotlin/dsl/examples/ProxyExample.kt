package dsl.examples

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ProxyExample {

    @Test
    fun main() {
        val client = SKRole("Client")
        val proxy = SKRole("Proxy")
        val server = SKRole("Server")

        val g = globalProtocolInternal {
            send<Request>(client, proxy)

            send<Request>(proxy, server)

            choice(server) {
                branch("Ok") {
                    send<Response>(server, proxy)
                    send<Response>(proxy, client)
                }
                branch("Error") {
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
                            LocalTypeSend(client, Response::class.java, LEnd, "Ok")
                        ),
                        "Error" to LocalTypeReceive(
                            server,
                            Error::class.java,
                            LocalTypeSend(client, Error::class.java, LEnd, "Error")
                        )
                    )
                )
            )
        )
        val lServer = LocalTypeReceive(
            proxy, Request::class.java,
            LocalTypeInternalChoice(
                mapOf(
                    "Ok" to LocalTypeSend(proxy, Response::class.java, LEnd, "Ok"),
                    "Error" to LocalTypeSend(proxy, Error::class.java, LEnd, "Error")
                )
            )
        )

        assertEquals(lClient, g.project(client))
        assertEquals(lProxy, g.project(proxy))
        assertEquals(lServer, g.project(server))
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