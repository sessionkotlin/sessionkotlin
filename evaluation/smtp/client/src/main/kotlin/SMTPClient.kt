import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import messages.*
import smtp.Server
import smtp.fluent.*

// Google SMTP server with optional TLS
const val host = "smtp.gmail.com"
const val port = 587

//const val host = "aspmx.l.google.com"
//const val port = 25

const val domain = "user.testing.com"

suspend fun main() {
    SKMPEndpoint(SMTPMsgFormatter()).use { e ->
        e.request(Server, host, port)
        SMTPClient1(e)
            .branch()
            .let { b1 ->
                when (b1) {
                    is SMTPClient2_220 -> b1
                        .receiveFromServer(SKBuffer())
                        .let { doEhlo(it) }
                    is SMTPClient31_554 -> b1.receiveFromServer(SKBuffer())
                }
            }
    }
}

suspend fun doEhlo(s3: SMTPClient3_Interface) {
    println("ehlo")
    s3.branchehlo()
        .sendToServer(Ehlo(domain))
        .let {
            var b5 = it
            do {
                when (val b6 = b5.branch()) {
                    is `SMTPClient6_250-` -> {
                        b5 = b6.receiveFromServer(SKBuffer())
                    }
                    is SMTPClient8_250 -> {
                        doTLS(b6.receiveFromServer(SKBuffer()))
                        break
                    }
                }
            } while (true)
        }
}

suspend fun doTLS(it: SMTPClient9_Interface) {
    val b14 = it.sendToServer(TLS())
        .receiveFromServer(SKBuffer())
        .branchehlo()
        .sendToServer(Ehlo(domain))
        .branch()
    when(b14) {
        is SMTPClient20_quit -> b14.receiveFromServer(SKBuffer())
    }
}
