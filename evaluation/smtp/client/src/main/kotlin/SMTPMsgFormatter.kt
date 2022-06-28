import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.backend.message.SKMessageFormatter
import com.github.d_costa.sessionkotlin.backend.message.SKPayload
import messages.*
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*

fun ByteBuffer.customString(): ByteArray = Arrays.copyOfRange(array(), position(), limit())

class SMTPMsgFormatter : SKMessageFormatter {
    companion object {
        val charset = Charsets.UTF_8
    }

    override fun fromBytes(b: ByteBuffer): Optional<SKMessage> {
//        println(String(b.customString(), charset))

        if (b.remaining() < 4) {
            Arrays.copyOfRange(b.array(), b.position(), b.limit())
            return Optional.empty()
        }

        val head = String(Arrays.copyOfRange(b.array(), b.position(), 4), charset)

        b.position(b.position() + 4)

        if (!validSMTPCode(head))
            throw InvalidSMTPCode(head)

        val body = readOneLine(b)

        val msg = when (head.trim()) {
            Code.C220 -> C220(body)
            Code.C221 -> C221(body)
            Code.C250 -> C250(body)
            Code.C250Hyphen -> C250Hyphen(body)
            Code.C554 -> C554(body)
            else -> throw NotImplementedSMTPCode(head)
        }
        return Optional.of(SKPayload(msg, msg.code))
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

    override fun toBytes(msg: SKMessage): ByteArray = (msg as SMTPMessage).toBytes(charset)

}