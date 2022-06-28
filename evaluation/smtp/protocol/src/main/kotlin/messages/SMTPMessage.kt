package messages

import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import java.nio.charset.Charset

abstract class SMTPMessage(val code: String, open val body: String) {

    companion object {
        const val CR: Char = (0x0D).toChar()
        const val LF: Char = (0x0A).toChar()
    }

    fun toBytes(charset: Charset): ByteArray {
        return ("$code $body $CR$LF").toByteArray(charset)
    }

    override fun toString(): String = "<$code, $body>"
}
