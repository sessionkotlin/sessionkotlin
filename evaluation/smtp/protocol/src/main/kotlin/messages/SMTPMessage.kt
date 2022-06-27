package messages

import com.github.d_costa.sessionkotlin.backend.message.SKMessage

abstract class SMTPMessage(private val code: String, private val body: String): SKMessage {

    companion object {
        const val CLRF = "\r\n"
    }

    fun toBytes(): ByteArray {
        return ("$code $body $CLRF").toByteArray()
    }

}