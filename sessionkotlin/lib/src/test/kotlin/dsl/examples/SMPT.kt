package examples

import org.david.sessionkotlin_lib.dsl.GlobalEnv
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test

class SMPT {

    companion object {
        val c = Role("C")
        val s = Role("S")
    }

    @Test
    fun main() {
        globalProtocol {
            send<Code220>(s, c)
            exec(ehlo)
        }
    }

    private fun ehloAux(continuation: GlobalEnv) =
        globalProtocol {
            choice(s) {
                case("250") {
                    send<Code250d>(s, c)
                    rec()
                }
                case("250d") {
                    send<Code250>(s, c)
                    exec(continuation)
                }
            }
        }

    private val mailAuxInner = globalProtocol {
        choice(c) {
            case("Data") {
                send<Dataline>(c, s)
                rec()
            }
            case("Subject") {
                send<Subject>(c, s)
                rec()
            }
            case("End") {
                send<EndOfData>(c, s)
                send<Code250>(s, c)
            }
        }
    }
    private val mailAux = globalProtocol {
        choice(c) {
            case("Recipient") {
                send<Recipient>(c, s)
                send<Code250>(s, c)
                rec()
            }
            case("Data") {
                send<Data>(c, s)
                send<Code354>(s, c)
                exec(mailAuxInner)
            }
        }
    }
    private val mail = globalProtocol {

        choice(c) {
            case("") {
                send<Mail>(c, s)
                choice(s) {
                    case("501") {
                        send<Code501>(s, c)
                        rec()
                    }
                    case("250") {
                        send<Code250>(s, c)
                        exec(mailAux)
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
                        rec()
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
                exec(ehloAux(auth))
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
                exec(ehloAux(startTLS))

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