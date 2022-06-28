package messages

class C220(body: String): SMTPMessage(Code.C220, body)
class C221(body: String): SMTPMessage(Code.C221, body)
class C554(body: String): SMTPMessage(Code.C554, body)
class C250(body: String): SMTPMessage(Code.C250, body)
class C250Hyphen(body: String): SMTPMessage(Code.C250Hyphen, body)
