package messages

class Quit: SMTPMessage(Code.Quit, "")
class Ehlo(domain: String): SMTPMessage(Code.Ehlo, domain)
class TLS: SMTPMessage(Code.TLS, "")
