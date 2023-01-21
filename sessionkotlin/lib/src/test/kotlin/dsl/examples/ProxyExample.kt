package dsl.examples

import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.globalProtocolInternal
import com.github.sessionkotlin.lib.dsl.types.*
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
                    LocalTypeReceive(proxy, Response::class.java, MsgLabel("ok"), LEnd),
                    LocalTypeReceive(proxy, Error::class.java, MsgLabel("error"), LEnd)
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
                            Response::class.java, MsgLabel("ok"),
                            LocalTypeSend(client, Response::class.java, MsgLabel("ok"), LEnd)
                        ),
                        LocalTypeReceive(
                            server,
                            Error::class.java, MsgLabel("error"),
                            LocalTypeSend(client, Error::class.java, MsgLabel("error"), LEnd)
                        )
                    )
                )
            )
        )
        val lServer = LocalTypeReceive(
            proxy, Request::class.java,
            LocalTypeInternalChoice(
                listOf(
                    LocalTypeSend(proxy, Response::class.java, MsgLabel("ok"), LEnd),
                    LocalTypeSend(proxy, Error::class.java, MsgLabel("error"), LEnd)
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
