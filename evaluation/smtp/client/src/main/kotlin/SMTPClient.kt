import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import messages.C220
import messages.EHLO
import smtp.Server
import smtp.fluent.SMTPClient1
import smtp.fluent.SMTPClient5_250
import smtp.fluent.SMTPClient7_250hyphen

suspend fun main() {
    // Google SMTP server with optional TLS
    val host = "smtp.gmail.com"
    val port = 587

    val domain = "user.testing.com"

    SKMPEndpoint(SMTPMsgFormatter()).use { e ->
        val buf220 = SKBuffer<C220>()
        e.request(Server, host, port)
        SMTPClient1(e)
            .receiveFromServer(buf220)
            .also { println(buf220.value) }
            .branchQuit()
//            .branchContinue()
//            .sendToServer(EHLO(domain))
//            .branch()
//            .let {
//                when(it) {
//                    is SMTPClient5_250 -> TODO()
//                    is SMTPClient7_250hyphen -> TODO()
//                }
//            }
    }


}