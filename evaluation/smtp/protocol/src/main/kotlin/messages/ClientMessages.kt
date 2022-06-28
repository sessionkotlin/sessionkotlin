package messages

class Quit(body: String): SMTPMessage(Code.Quit, body)
class Ehlo(body: String): SMTPMessage(Code.Ehlo, body)
