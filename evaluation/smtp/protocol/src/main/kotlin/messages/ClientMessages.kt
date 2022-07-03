package messages

class Quit: SMTPMessage(Code.Quit, "")
class Ehlo(domain: String): SMTPMessage(Code.Ehlo, domain)
class Mail(sender: String): SMTPMessage(Code.Mail, "from:<$sender>")
class RCPT(recipient: String): SMTPMessage(Code.RCPT, "to:<$recipient>")
class Data: SMTPMessage(Code.Data, "")
class DataLine(line: String): SMTPMessage("",line)
class DataOver: SMTPMessage("","${SMTPMessage.CR}${SMTPMessage.LF}.") // serializer adds termination
class MessageIdHeader(id: String): SMTPMessage("","message-id:<$id>")
class FromHeader(id: String): SMTPMessage("","from:<$id>")
class ToHeader(id: String): SMTPMessage("","to:<$id>")
