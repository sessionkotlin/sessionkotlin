import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.endpoint.ConnectionEnd
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.tls.TLSSocketWrapper
import messages.*
import smtp.Server
import smtp.fluent.*
import java.io.File
import java.io.FileInputStream
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

val props = Properties()
val envFile = File("mail.env")

@Suppress("unused")
val d = props.load(FileInputStream(envFile))

val host = props["host"] as String
val port = (props["port"] as String).toInt()

val domain = props["domain"] as String
val sender = props["sender"] as String
val recipient = props["recipient"] as String
val password = props["password"] as String

val subject = "Hello!"
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
    SKMPEndpoint(SMTPMsgFormatter(), debug = true).use { e ->
        e.request(Server, host, port)
        SMTPClient1(e)
            .branch()
            .let { b1 ->
                when (b1) {
                    is SMTPClient1_220Interface -> b1
                        .receive220FromServer { }
                        .let { doEhlo(e, it) }
                    is SMTPClient1_554Interface -> b1.receive554FromServer { }
                }
            }
    }
}

suspend fun doEhlo(e: SKMPEndpoint, s: SMTPClient2Interface) {
    s.sendEhloToServer(Ehlo(domain))
        .let {
            var s3Branch: SMTPClient3Branch = it.branch()

            while (true) {
                s3Branch = when (s3Branch) {
                    is `SMTPClient3_250-Interface` -> {
                        s3Branch
                            .`receive250-FromServer` { }
                            .branch()
                    }
                    is SMTPClient3_250Interface -> {
                        doTLS(e, s3Branch.receive250FromServer { })
                        break
                    }
                }
            }
        }
}

suspend fun doTLS(e: SKMPEndpoint, s: SMTPClient4Interface) {
    val s6 = s
        .sendStartTLSToServer(StartTLS())
        .receive220FromServer { }

    e.wrap(Server, TLSSocketWrapper(ConnectionEnd.Client, debug = true))

    doSecureEhlo(s6)
}


suspend fun doSecureEhlo(s: SMTPClient6Interface) {
    s.sendEhloToServer(Ehlo(domain))
        .let {
            var s7Branch: SMTPClient7Branch = it.branch()

            // Server will send a 250 message with the supported Auth methods
            while (true) {
                s7Branch = when (s7Branch) {
                    is `SMTPClient7_250-Interface` -> {
                        s7Branch
                            .`receive250-FromServer` { }
                            .branch()
                    }
                    is SMTPClient7_250Interface -> {
                        doAuth(s7Branch.receive250FromServer { })
                        break
                    }
                }
            }
        }
}

suspend fun doAuth(s: SMTPClient8Interface) {
    val b = s.sendAuthToServer(AuthLogin())
        .receiveFromServer { }
        .sendToServer(AuthUsername(sender))
        .receiveFromServer { }
        .sendToServer(AuthPassword(password))
        .branch()


    when (b) {
        is SMTPClient13_535Interface -> b.receive535FromServer { }
        is SMTPClient13_538Interface -> b.receive538FromServer { }
        is SMTPClient13_235Interface -> doMail(b.receive235FromServer { })
        is SMTPClient13_504Interface -> b.receive504FromServer { }
        is SMTPClient13_501Interface -> b.receive501FromServer { }
        is `SMTPClient13_535-Interface` -> consume535(b.`receive535-FromServer` { })
        is `SMTPClient13_534-Interface` -> consume534(b.`receive534-FromServer` { })
        is SMTPClient13_534Interface -> b.receive534FromServer { }
    }


}

suspend fun consume534(s: SMTPClient28Interface) {
    var b = s.branch()

    do {
        var done = true
        when(b) {
            is `SMTPClient28_534-Interface` -> {
                b = b.`receive534-FromServer` { }.branch()
                done = false
            }
            is SMTPClient28_534Interface -> b.receive534FromServer { }
        }
    } while (!done)
}

suspend fun consume535(s: SMTPClient27Interface) {
    var b = s.branch()

    do {
        var done = true
        when(b) {
            is `SMTPClient27_535-Interface` -> {
                b = b.`receive535-FromServer` { }.branch()
                done = false
            }
            is SMTPClient27_535Interface -> b.receive535FromServer { }
        }
    } while (!done)
}


suspend fun doMail(s: SMTPClient14Interface) {
    val s9 = s.sendMailToServer(Mail(sender))
        .branch()
    when (s9) {
        is SMTPClient15_530Interface -> s9.receive530FromServer { }
        is SMTPClient15_553Interface -> s9.receive553FromServer { }
        is SMTPClient15_250Interface -> s9.receive250FromServer { }
            .sendRcptToServer(RCPT(recipient))
            .branch()
            .let {rcptBranch ->
                when (rcptBranch) {
                    is SMTPClient17_550Interface -> rcptBranch.receive550FromServer { }
                    is `SMTPClient17_550-Interface` -> consumeRCPT550(rcptBranch)
                    is SMTPClient17_250Interface -> rcptBranch.receive250FromServer { }
                        .sendDataToServer(Data())
                        .receive354FromServer { }
                        .sendToServer(MessageIdHeader(messageId()))
                        .sendToServer(FromHeader(sender))
                        .sendToServer(ToHeader(recipient))
                        .sendToServer(SubjectHeader(subject))
                        .let { addMailBody(it) }
                        .sendDataOverToServer(DataOver())
                        .branch()
                        .let {
                            when (it) {
                                is SMTPClient25_250Interface -> it.receive250FromServer { }
                                is `SMTPClient25_550-Interface` -> consumeData550(it)
                                is SMTPClient25_550Interface -> it.receive550FromServer { }
                            }
                        }
                }
            }
    }
}

suspend fun addMailBody(s: SMTPClient24Interface): SMTPClient24Interface {
    var b = s.sendDataLineToServer(DataLine(mailBody.first()))

    for (l in mailBody.subList(1, mailBody.size)) {
        b = b.sendDataLineToServer(DataLine(l))
    }

    return b
}

suspend fun consumeData550(s: `SMTPClient25_550-Interface`) {
    s.`receive550-FromServer` { }
        .let {
            var b = it.branch()
            do {
                when (b) {
                    is `SMTPClient26_550-Interface` -> b = b.`receive550-FromServer` { }.branch()
                    is SMTPClient26_550Interface -> {
                        b.receive550FromServer { }
                        break
                    }
                }
            } while (true)
        }
}

suspend fun consumeRCPT550(s: `SMTPClient17_550-Interface`) {
    s.`receive550-FromServer` { }
        .let {
            var b = it.branch()
            do {
                when (b) {
                    is `SMTPClient18_550-Interface` -> b = b.`receive550-FromServer` { }.branch()
                    is SMTPClient18_550Interface -> {
                        b.receive550FromServer { }
                        break
                    }
                }
            } while (true)
        }
}
