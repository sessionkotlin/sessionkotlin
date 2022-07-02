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
                branch {
                    send<Response>(server, proxy, "ok")
                    send<Response>(proxy, client, "ok")
                }
                branch {
                    send<Error>(server, proxy, "error")
                    send<Error>(proxy, client, "error")
                }
            }
        }

        val lClient = LocalTypeSend(
            proxy, Request::class.java,
            LocalTypeExternalChoice(
                proxy,
                listOf(
                    LocalTypeReceive(proxy, Response::class.java, LEnd),
                    LocalTypeReceive(proxy, Error::class.java, LEnd)
                )
            )
        )
        val lProxy = LocalTypeReceive(
            client, Request::class.java,
            LocalTypeSend(
                server, Request::class.java,
                LocalTypeExternalChoice(
                    server,
                    listOf(
                        LocalTypeReceive(
                            server,
                            Response::class.java,
                            LocalTypeSend(client, Response::class.java, LEnd)
                        ),
                        LocalTypeReceive(
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
                listOf(
                    LocalTypeSend(proxy, Response::class.java, LEnd),
                    LocalTypeSend(proxy, Error::class.java, LEnd)
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
