import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import messages.C220
import messages.C554
import messages.Ehlo
import messages.SMTPMessage
import smtp.Server
import smtp.fluent.*

suspend fun main() {
    // Google SMTP server with optional TLS
    val host = "aspmx.l.google.com"
    val port = 25

    val domain = "user.testing.com"

    SKMPEndpoint(SMTPMsgFormatter()).use { e ->
        e.request(Server, host, port)
        SMTPClient1(e)
            .branch()
            .let { b1 ->
                val buf554 = SKBuffer<C554>()
                val buf220 = SKBuffer<C220>()
                when(b1) {
                    is SMTPClient15_554 -> b1
                        .receiveFromServer(buf554).also { println(buf554.value) }
                    is SMTPClient2_220 -> b1
                        .receiveFromServer(buf220).also {
                            println(buf220.value)
//                            println(buf220.value.body)
                        }
                        .branchquit()
                }
            }
    }
}