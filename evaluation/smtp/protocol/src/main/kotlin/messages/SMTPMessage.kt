package messages

import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import java.nio.charset.Charset

abstract class SMTPMessage(val code: String, open val body: String): SKMessage(body, code) {

    companion object {
        const val CR: Char = (0x0D).toChar()
        const val LF: Char = (0x0A).toChar()
    }

    override fun toString(): String  {
        return if (body.isEmpty())
            "<$code>"
        else if (code.length > 3 && code[code.length - 1] == '-')
            "<$code$body>"
        else
            "<$code $body>"
    }
}
