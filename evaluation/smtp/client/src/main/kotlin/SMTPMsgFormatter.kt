import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.backend.message.SKMessageFormatter
import messages.*
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*

class SMTPMsgFormatter : SKMessageFormatter {


    override fun fromBytes(b: ByteBuffer): Optional<SKMessage> {

        if (b.remaining() < 4) {
            // No code: wait for more bytes
            return Optional.empty()
        }

        // Extract 4 first bytes
        val head = String(Arrays.copyOfRange(b.array(), b.position(), b.position() + 4), SMTPMessage.charset)
        // Update position
        b.position(b.position() + 4)

        if (!validSMTPCode(head)) {
            throw InvalidSMTPCode(head)
        }

        val body = readOneLine(b)

        val msg = when (head.trim()) {
            Code.C220 -> C220(body)
            Code.C221 -> C221(body)
            Code.C235 -> C235(body)
            Code.C250 -> C250(body)
            Code.C250Hyphen -> C250Hyphen(body)

            Code.C334 -> C334(body.decodeBase64())
            Code.C354 -> C354(body)

            Code.C501 -> C501(body)
            Code.C504 -> C504(body)
            Code.C530 -> C530(body)
            Code.C534 -> C534(body)
            Code.C534Hyphen -> C534Hyphen(body)
            Code.C535 -> C535(body)
            Code.C535Hyphen -> C535Hyphen(body)
            Code.C538 -> C538(body)
            Code.C550 -> C550(body)
            Code.C550Hyphen -> C550Hyphen(body)
            Code.C554 -> C554(body)
            else -> throw NotImplementedSMTPCode(head)
        }
        return Optional.of(SKMessage(msg.code, msg))
    }

    class NotImplementedSMTPCode(code: String) : RuntimeException("Code not implemented: $code")
    class InvalidSMTPCode(code: String) : RuntimeException("Invalid code: $code")

    private fun validSMTPCode(head: String): Boolean {
        if (head.length < 3)
            return false

        val firstDigits = listOf('2', '3', '4', '5')
        val secondDigits = listOf('0', '1', '2', '3', '4', '5')
        val thirdDigits = '0'..'9'
        val fourthChars = listOf(' ', '-')

        if (head[0] !in firstDigits
            || head[1] !in secondDigits
            || head[2] !in thirdDigits
            || (head.length > 3 && head[3] !in fourthChars)
        ) {
            return false
        }
        return true
    }

    private fun readOneLine(b: ByteBuffer): String {
        val body = StringBuilder()

        while (b.hasRemaining()) {
            val c: Char = b.get().toInt().toChar()

            if (c == SMTPMessage.LF) {
                if (body.isNotEmpty() && body.last() == SMTPMessage.CR) {
                    // CRLF detected: Pop LF
                    body.delete(body.length - 1, body.length)
                    break
                }
            }
            body.append(c)
        }
        return body.toString()
    }

    override fun toBytes(msg: SKMessage): ByteArray {
        return serialize(msg.payload as SMTPMessage)
    }

    private fun serialize(msg: SMTPMessage): ByteArray {
        val termination = "${SMTPMessage.CR}${SMTPMessage.LF}"
        val content = listOf(msg.code, msg.body).joinToString(" ").trim()
        return ("$content$termination").toByteArray(SMTPMessage.charset)
    }
}