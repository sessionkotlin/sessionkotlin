package messages

import java.util.*

class Quit: SMTPMessage(Code.Quit)
class Ehlo(domain: String): SMTPMessage(Code.Ehlo, domain)
class Mail(sender: String): SMTPMessage(Code.Mail, "from:<$sender>")
class RCPT(recipient: String): SMTPMessage(Code.RCPT, "to:<$recipient>")
class Data: SMTPMessage(Code.Data)
class DataLine(line: String): SMTPMessage("",line)
class DataOver: SMTPMessage("","${SMTPMessage.CR}${SMTPMessage.LF}.") // serializer adds termination
class MessageIdHeader(id: String): SMTPMessage("","message-id:<$id>")
class FromHeader(id: String): SMTPMessage("","from:<$id>")
class ToHeader(id: String): SMTPMessage("","to:<$id>")
class StartTLS: SMTPMessage(Code.TLS)
class AuthLogin: SMTPMessage(Code.Auth, "LOGIN")
class AuthUsername(username: String): SMTPMessage("", username.encodeBase64())
class AuthPassword(password: String): SMTPMessage("", password.encodeBase64())

fun String.encodeBase64(): String = Base64.getEncoder().encodeToString(this.toByteArray(SMTPMessage.charset))
fun String.decodeBase64(): String = String(Base64.getDecoder().decode(this), SMTPMessage.charset)
