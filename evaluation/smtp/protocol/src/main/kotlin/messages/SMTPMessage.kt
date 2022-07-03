package messages

import com.github.d_costa.sessionkotlin.backend.message.SKMessage

abstract class SMTPMessage(val code: String, open val body: String): SKMessage(body, code) {

    companion object {
        const val CR: Char = (0x0D).toChar()
        const val LF: Char = (0x0A).toChar()
    }

    override fun toString(): String {
        val separator = if (code.length > 3 && code[code.length - 1] == '-')
            "" else " "
        return listOf(code, body).joinToString(separator).trim()
    }
}
