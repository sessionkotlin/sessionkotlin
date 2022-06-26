import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol
import com.github.d_costa.sessionkotlin.dsl.types.*

class Code220
class Code221
class Code235
class Code250
class Code250d
class Code354
class Code501
class Code535
class Ehlo
class Auth
class Mail
class Recipient
class Data
class Dataline
class Subject
class EndOfData

fun main() {

    val c = SKRole("C")
    val s = SKRole("S")
    lateinit var tEhlo: RecursionTag
    lateinit var tsecureEhlo: RecursionTag
    lateinit var tAuth: RecursionTag
    lateinit var tMail1: RecursionTag
    lateinit var tMail2: RecursionTag
    lateinit var tMail3: RecursionTag


    val mail: GlobalProtocol = {
        tMail1 = mu()

        choice(c) {
            branch("Mail") {
                send<Mail>(c, s)
                choice(s) {
                    branch("Quit") {
                        send<Code501>(s, c)
                        goto(tMail1)
                    }
                    branch("250") {
                        send<Code250>(s, c)
                        tMail2 = mu()
                        choice(c) {
                            branch("Recipient") {
                                send<Recipient>(c, s)
                                send<Code250>(s, c)
                                goto(tMail2)
                            }
                            branch("Data") {
                                send<Data>(c, s)
                                send<Code354>(s, c)
                                tMail3 = mu()

                                choice(c) {
                                    branch("Data") {
                                        send<Dataline>(c, s)
                                        goto(tMail3)
                                    }
                                    branch("Subject") {
                                        send<Subject>(c, s)
                                        goto(tMail3)
                                    }
                                    branch("End") {
                                        send<EndOfData>(c, s)
                                        send<Code250>(s, c)
                                        goto(tMail1)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            branch("Quit") {
                send<Unit>(c, s)
                send<Code221>(s, c)
            }
        }
    }
    val auth: GlobalProtocol = {
        tAuth = mu()
        choice(c) {
            branch("Continue") {
                send<Auth>(c, s)
                choice(s) {
                    branch("235") {
                        send<Code235>(s, c)
                        mail()
                    }
                    branch("535") {
                        send<Code535>(s, c)
                        goto(tAuth)
                    }
                }
            }
            branch("Quit") {
                send<Unit>(c, s)
            }
        }
    }
    val secureEhlo: GlobalProtocol = {
        choice(c) {
            branch("Continue") {
                send<Ehlo>(c, s)
                tsecureEhlo = mu()

                choice(s) {
                    branch("250") {
                        send<Code250d>(s, c)
                        goto(tsecureEhlo)
                    }
                    branch("250d") {
                        send<Code250>(s, c)
                        auth()
                    }
                }
            }
            branch("Quit") {
                send<Unit>(c, s)
            }
        }
    }
    val startTLS: GlobalProtocol = {
        choice(c) {
            branch("Continue") {
                send<Unit>(c, s)
                send<Code220>(s, c)
                // Do TLS handshake here
                secureEhlo()
            }
            branch("Quit") {
                send<Unit>(c, s)
            }
        }
    }
    val ehlo: GlobalProtocol = {
        choice(c) {
            branch("Continue") {
                send<Ehlo>(c, s)
                tEhlo = mu()

                choice(s) {
                    branch("250") {
                        send<Code250d>(s, c)
                        goto(tEhlo)
                    }
                    branch("250d") {
                        send<Code250>(s, c)
                        startTLS()
                    }
                }
            }
            branch("Quit") {
                send<Unit>(c, s)
            }
        }
    }

    globalProtocol("SMTP") {
        send<Code220>(s, c)
        ehlo()
    }


}