import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.backend.message.SKMessageFormatter
import messages.SMTPMessage
import java.nio.ByteBuffer

class SMTPMsgFormatter: SKMessageFormatter {
    override fun toBytes(msg: SKMessage): ByteArray = (msg as SMTPMessage).toBytes()

    override fun fromBytes(b: ByteArray): SKMessage {
        println(b.toString())





        TODO("Not yet implemented")
    }

}