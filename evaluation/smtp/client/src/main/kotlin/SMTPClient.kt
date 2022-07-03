import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import messages.*
import smtp.Server
import smtp.fluent.*
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

val props = Properties()
val envFile = File("mail.env")
val d = props.load(java.io.FileInputStream(envFile))

val host = props["host"] as String
val port = (props["port"] as String).toInt()

val domain = props["domain"] as String
val sender = props["sender"] as String
val recipient = props["recipient"] as String

val mailBody = listOf(
    "Hello world,",
    "",
    "Lorem ipsum dolor sit amet, consectetur adipiscing elit.",
    "Cras hendrerit posuere augue, ut pulvinar nulla semper ut. Morbi sed metus nec tortor efficitur tempus."
)

private fun messageId(): String {
    val time = System.currentTimeMillis()
    val md = MessageDigest.getInstance("MD5")
    val md5sum = md.digest("$sender$recipient".toByteArray())
    val md5String = String.format("%032X", BigInteger(1, md5sum))
    return "$time$md5String@$domain"
}

suspend fun main() {
    SKMPEndpoint(SMTPMsgFormatter(), logMessages = true).use { e ->
        e.request(Server, host, port)
        SMTPClient1(e)
            .branch()
            .let { b1 ->
                when (b1) {
                    is SMTPClient1_220Interface -> b1
                        .receive220FromServer(SKBuffer())
                        .let { doEhlo(it) }
                    is SMTPClient1_554Interface -> b1.receive554FromServer(SKBuffer())
                }
            }
    }
}

suspend fun doEhlo(s3: SMTPClient3Interface) {
    s3.sendehloToServer(Ehlo(domain))
        .let {
            when (val b5 = it.branch()) {
                is `SMTPClient5_250-Interface` -> {
                    read250HLines(b5.`receive250-FromServer`(SKBuffer()))
                }
                is SMTPClient5_250Interface -> {
                    doMail(b5.receive250FromServer(SKBuffer()))
                }
            }
        }
}

suspend fun read250HLines(smtpClient8Interface: SMTPClient8Interface) {
    var b = smtpClient8Interface
    do {
        when (val b8 = b.branch()) {
            is `SMTPClient8_250-Interface` -> b = b8.`receive250-FromServer`(SKBuffer())
            is SMTPClient8_250Interface -> {
                doMail(b8.receive250FromServer(SKBuffer()))
                break
            }
        }
    } while (true)
}

suspend fun doMail(b10: SMTPClient10Interface) {
    val b11 = b10.sendmailToServer(Mail(sender))
        .branch()
    when (b11) {
        is SMTPClient11_553Interface -> b11.receive553FromServer(SKBuffer())
        is SMTPClient11_250Interface -> b11.receive250FromServer(SKBuffer())
            .sendrcptToServer(RCPT(recipient))
            .branch()
            .let {
                when (it) {
                    is SMTPClient16_550Interface -> it.receive550FromServer(SKBuffer())
                    is `SMTPClient16_550-Interface` -> consumeRCPT550(it)
                    is SMTPClient16_250Interface -> it.receive250FromServer(SKBuffer())
                        .senddataToServer(Data())
                        .receive354FromServer(SKBuffer())
                        .sendToServer(MessageIdHeader(messageId()))
                        .sendToServer(FromHeader(sender))
                        .sendToServer(ToHeader(recipient))
//                        .let { addMailBody(it) }
//                        .senddataOverToServer(DataOver())
//                        .branch()
//                        .let { when(it) {
//                            is SMTPClient34_250Interface -> it.receive250FromServer(SKBuffer())
//                            is `SMTPClient34_550-Interface` -> consumeData550(it)
//                            is SMTPClient34_550Interface -> it.receive550FromServer(SKBuffer())
//                        } }
                }
            }
    }
}

suspend fun addMailBody(b29: SMTPClient29Interface): SMTPClient32Interface {
    var b = b29.senddataLineToServer(DataLine(mailBody.first()))

    for (l in mailBody.subList(1, mailBody.size)) {
        b = b.senddataLineToServer(DataLine(l))
    }

    return b
}

suspend fun consumeData550(b31: `SMTPClient34_550-Interface`) {
    b31.`receive550-FromServer`(SKBuffer())
        .let {
            var b = it.branch()
            do {
                when (b) {
                    is `SMTPClient39_550-Interface` -> b = b.`receive550-FromServer`(SKBuffer()).branch()
                    is SMTPClient39_550Interface -> {
                        b.receive550FromServer(SKBuffer())
                        break
                    }
                }
            } while (true)
        }
}

suspend fun consumeRCPT550(b16: `SMTPClient16_550-Interface`) {
    b16.`receive550-FromServer`(SKBuffer())
        .let {
            var b = it.branch()
            do {
                when (b) {
                    is `SMTPClient23_550-Interface` -> b = b.`receive550-FromServer`(SKBuffer()).branch()
                    is SMTPClient23_550Interface -> {
                        b.receive550FromServer(SKBuffer())
                        break
                    }
                }
            } while (true)
        }
}
