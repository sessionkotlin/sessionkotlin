package messages

class QUIT: SMTPMessage(Code.Quit, "")
class EHLO(domain: String): SMTPMessage(Code.EHLO, domain)
