package lib.examples

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.david.sessionkotlin_lib.dsl.types.asString
import org.junit.jupiter.api.Test

class SMPT {

    companion object {
        val c = Role("C")
        val s = Role("S")
    }

    @Test
    fun main() {
        val g = globalProtocol {
            send<Code220>(s, c)
            exec(ehlo)
        }
        g.project(s).asString()
        g.project(c).asString()
    }

    private val mail = globalProtocol {
        val tMail = miu("tMail")

        choice(c) {
            case("") {
                send<Mail>(c, s)
                choice(s) {
                    case("501") {
                        send<Code501>(s, c)
                        goto(tMail)
                    }
                    case("250") {
                        send<Code250>(s, c)
                        val tMailAux = miu("tMailAux")
                        choice(c) {
                            case("Recipient") {
                                send<Recipient>(c, s)
                                send<Code250>(s, c)
                                goto(tMailAux)
                            }
                            case("Data") {
                                send<Data>(c, s)
                                send<Code354>(s, c)
                                val tMailAuxInner = miu("tMailAuxInner")

                                choice(c) {
                                    case("Data") {
                                        send<Dataline>(c, s)
                                        goto(tMailAuxInner)
                                    }
                                    case("Subject") {
                                        send<Subject>(c, s)
                                        goto(tMailAuxInner)
                                    }
                                    case("End") {
                                        send<EndOfData>(c, s)
                                        send<Code250>(s, c)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            case("Quit") {
                send<Unit>(c, s)
                send<Code221>(s, c)
            }
        }
    }
    private val auth = globalProtocol {
        val tAuth = miu("tAuth")
        choice(c) {
            case("Continue") {
                send<Auth>(c, s)
                choice(s) {
                    case("235") {
                        send<Code235>(s, c)
                        exec(mail)
                    }
                    case("535") {
                        send<Code535>(s, c)
                        goto(tAuth)
                    }
                }
            }
            case("Quit") {
                send<Unit>(c, s)
            }
        }
    }
    private val secureEhlo = globalProtocol {
        choice(c) {
            case("Continue") {
                send<Ehlo>(c, s)
                val tEhloAux = miu("tEhloAux")

                choice(s) {
                    case("250") {
                        send<Code250d>(s, c)
                        goto(tEhloAux)
                    }
                    case("250d") {
                        send<Code250>(s, c)
                        exec(auth)
                    }
                }
            }
            case("Quit") {
                send<Unit>(c, s)
            }
        }
    }

    private val startTLS = globalProtocol {
        choice(c) {
            case("Continue") {
                send<Unit>(c, s)
                send<Code220>(s, c)
                exec(secureEhlo)
            }
            case("Quit") {
                send<Unit>(c, s)
            }
        }
    }
    private val ehlo = globalProtocol {
        choice(c) {
            case("Continue") {
                send<Ehlo>(c, s)
                val tEhloAux = miu("tEhloAux")

                choice(s) {
                    case("250") {
                        send<Code250d>(s, c)
                        goto(tEhloAux)
                    }
                    case("250d") {
                        send<Code250>(s, c)
                        exec(startTLS)
                    }
                }
            }
            case("Quit") {
                send<Unit>(c, s)
            }
        }
    }

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
}
