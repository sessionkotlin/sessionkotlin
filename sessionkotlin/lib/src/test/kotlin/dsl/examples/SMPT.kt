package dsl.examples

import dsl.util.UnitClass
import org.david.sessionkotlin_lib.dsl.RecursionTag
import org.david.sessionkotlin_lib.dsl.SKRole
import org.david.sessionkotlin_lib.dsl.globalProtocolInternal
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class SMPT {

    companion object {
        val c = SKRole("C")
        val s = SKRole("S")
        lateinit var tEhlo: RecursionTag
        lateinit var tsecureEhlo: RecursionTag
        lateinit var tAuth: RecursionTag
        lateinit var tMail1: RecursionTag
        lateinit var tMail2: RecursionTag
        lateinit var tMail3: RecursionTag
    }

    @Test
    fun main() {
        val g = globalProtocolInternal {
            send<Code220>(s, c)
            exec(ehlo)
        }
        assertEquals(g.project(s), lS)
        assertEquals(g.project(c), lC)
    }

    private val mail = globalProtocolInternal {
        tMail1 = miu("tMail1")

        choice(c) {
            case("Mail") {
                send<Mail>(c, s)
                choice(s) {
                    case("Quit") {
                        send<Code501>(s, c)
                        goto(tMail1)
                    }
                    case("250") {
                        send<Code250>(s, c)
                        tMail2 = miu("tMail2")
                        choice(c) {
                            case("Recipient") {
                                send<Recipient>(c, s)
                                send<Code250>(s, c)
                                goto(tMail2)
                            }
                            case("Data") {
                                send<Data>(c, s)
                                send<Code354>(s, c)
                                tMail3 = miu("tMail3")

                                choice(c) {
                                    case("Data") {
                                        send<Dataline>(c, s)
                                        goto(tMail3)
                                    }
                                    case("Subject") {
                                        send<Subject>(c, s)
                                        goto(tMail3)
                                    }
                                    case("End") {
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
            case("Quit") {
                send<Unit>(c, s)
                send<Code221>(s, c)
            }
        }
    }
    private val auth = globalProtocolInternal {
        tAuth = miu("tAuth")
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
    private val secureEhlo = globalProtocolInternal {
        choice(c) {
            case("Continue") {
                send<Ehlo>(c, s)
                tsecureEhlo = miu("tsecureEhlo")

                choice(s) {
                    case("250") {
                        send<Code250d>(s, c)
                        goto(tsecureEhlo)
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
    private val startTLS = globalProtocolInternal {
        choice(c) {
            case("Continue") {
                send<Unit>(c, s)
                send<Code220>(s, c)
                // Do TLS handshake here
                exec(secureEhlo)
            }
            case("Quit") {
                send<Unit>(c, s)
            }
        }
    }
    private val ehlo = globalProtocolInternal {
        choice(c) {
            case("Continue") {
                send<Ehlo>(c, s)
                tEhlo = miu("tEhlo")

                choice(s) {
                    case("250") {
                        send<Code250d>(s, c)
                        goto(tEhlo)
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

    // ********* LOCAL S *********//
    private val lMailS = LocalTypeRecursionDefinition(
        tMail1,
        LocalTypeExternalChoice(
            c,
            mapOf(
                "Mail" to LocalTypeReceive(
                    c,
                    Mail::class.java,
                    LocalTypeInternalChoice(
                        mapOf(
                            "Quit" to LocalTypeSend(c, Code501::class.java, LocalTypeRecursion(tMail1)),
                            "250" to LocalTypeSend(
                                c,
                                Code250::class.java,
                                LocalTypeRecursionDefinition(
                                    tMail2,
                                    LocalTypeExternalChoice(
                                        c,
                                        mapOf(
                                            "Recipient" to LocalTypeReceive(
                                                c,
                                                Recipient::class.java,
                                                LocalTypeSend(c, Code250::class.java, LocalTypeRecursion(tMail2))
                                            ),
                                            "Data" to LocalTypeReceive(
                                                c,
                                                Data::class.java,
                                                LocalTypeSend(
                                                    c,
                                                    Code354::class.java,
                                                    LocalTypeRecursionDefinition(
                                                        tMail3,
                                                        LocalTypeExternalChoice(
                                                            c,
                                                            mapOf(
                                                                "Data" to LocalTypeReceive(
                                                                    c,
                                                                    Dataline::class.java,
                                                                    LocalTypeRecursion(tMail3)
                                                                ),
                                                                "Subject" to LocalTypeReceive(
                                                                    c,
                                                                    Subject::class.java,
                                                                    LocalTypeRecursion(tMail3)
                                                                ),
                                                                "End" to LocalTypeReceive(
                                                                    c,
                                                                    EndOfData::class.java,
                                                                    LocalTypeSend(
                                                                        c,
                                                                        Code250::class.java,
                                                                        LocalTypeRecursion(tMail1)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                "Quit" to LocalTypeReceive(c, UnitClass, LocalTypeSend(c, Code221::class.java, LEnd))
            )
        )
    )
    private val lAuthS = LocalTypeRecursionDefinition(
        tAuth,
        LocalTypeExternalChoice(
            c,
            mapOf(
                "Continue" to LocalTypeReceive(
                    c,
                    Auth::class.java,
                    LocalTypeInternalChoice(
                        mapOf(
                            "235" to LocalTypeSend(c, Code235::class.java, lMailS),
                            "535" to LocalTypeSend(c, Code535::class.java, LocalTypeRecursion(tAuth))
                        )
                    )
                ),
                "Quit" to LocalTypeReceive(c, UnitClass, LEnd)
            )
        )
    )
    private val lSecureEhloS = LocalTypeExternalChoice(
        c,
        mapOf(
            "Continue" to LocalTypeReceive(
                c, Ehlo::class.java,
                LocalTypeRecursionDefinition(
                    tsecureEhlo,
                    LocalTypeInternalChoice(
                        mapOf(
                            "250" to LocalTypeSend(c, Code250d::class.java, LocalTypeRecursion(tsecureEhlo)),
                            "250d" to LocalTypeSend(c, Code250::class.java, lAuthS)
                        )
                    )
                )
            ),
            "Quit" to LocalTypeReceive(c, UnitClass, LEnd)
        )
    )
    private val lStartTLSS = LocalTypeExternalChoice(
        c,
        mapOf(
            "Continue" to LocalTypeReceive(
                c,
                UnitClass,
                LocalTypeSend(
                    c, Code220::class.java,
                    lSecureEhloS
                )
            ),
            "Quit" to LocalTypeReceive(c, UnitClass, LEnd)
        )
    )
    private val lEhloS = LocalTypeExternalChoice(
        c,
        mapOf(
            "Continue" to LocalTypeReceive(
                c, Ehlo::class.java,
                LocalTypeRecursionDefinition(
                    tEhlo,
                    LocalTypeInternalChoice(
                        mapOf(
                            "250" to LocalTypeSend(c, Code250d::class.java, LocalTypeRecursion(tEhlo)),
                            "250d" to LocalTypeSend(c, Code250::class.java, lStartTLSS)
                        )
                    )
                )
            ),
            "Quit" to LocalTypeReceive(c, UnitClass, LEnd)
        )
    )

    private val lS = LocalTypeSend(
        c, Code220::class.java,
        lEhloS
    )

    // ********* LOCAL C *********//
    private val lMailC = LocalTypeRecursionDefinition(
        tMail1,
        LocalTypeInternalChoice(
            mapOf(
                "Mail" to LocalTypeSend(
                    s,
                    Mail::class.java,
                    LocalTypeExternalChoice(
                        s,
                        mapOf(
                            "Quit" to LocalTypeReceive(s, Code501::class.java, LocalTypeRecursion(tMail1)),
                            "250" to LocalTypeReceive(
                                s,
                                Code250::class.java,
                                LocalTypeRecursionDefinition(
                                    tMail2,
                                    LocalTypeInternalChoice(
                                        mapOf(
                                            "Recipient" to LocalTypeSend(
                                                s,
                                                Recipient::class.java,
                                                LocalTypeReceive(s, Code250::class.java, LocalTypeRecursion(tMail2))
                                            ),
                                            "Data" to LocalTypeSend(
                                                s,
                                                Data::class.java,
                                                LocalTypeReceive(
                                                    s,
                                                    Code354::class.java,
                                                    LocalTypeRecursionDefinition(
                                                        tMail3,
                                                        LocalTypeInternalChoice(
                                                            mapOf(
                                                                "Data" to LocalTypeSend(
                                                                    s,
                                                                    Dataline::class.java,
                                                                    LocalTypeRecursion(tMail3)
                                                                ),
                                                                "Subject" to LocalTypeSend(
                                                                    s,
                                                                    Subject::class.java,
                                                                    LocalTypeRecursion(tMail3)
                                                                ),
                                                                "End" to LocalTypeSend(
                                                                    s, EndOfData::class.java,
                                                                    LocalTypeReceive(
                                                                        s,
                                                                        Code250::class.java,
                                                                        LocalTypeRecursion(tMail1)
                                                                    )
                                                                )
                                                            )
                                                        )
                                                    )
                                                )
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    )
                ),
                "Quit" to LocalTypeSend(s, UnitClass, LocalTypeReceive(s, Code221::class.java, LEnd))
            )
        )
    )
    private val lAuthC = LocalTypeRecursionDefinition(
        tAuth,
        LocalTypeInternalChoice(
            mapOf(
                "Continue" to LocalTypeSend(
                    s, Auth::class.java,
                    LocalTypeExternalChoice(
                        s,
                        mapOf(
                            "235" to LocalTypeReceive(s, Code235::class.java, lMailC),
                            "535" to LocalTypeReceive(s, Code535::class.java, LocalTypeRecursion(tAuth))
                        )
                    )
                ),
                "Quit" to LocalTypeSend(s, UnitClass, LEnd)
            )
        )
    )
    private val lSecureEhloC = LocalTypeInternalChoice(
        mapOf(
            "Continue" to LocalTypeSend(
                s, Ehlo::class.java,
                LocalTypeRecursionDefinition(
                    tsecureEhlo,
                    LocalTypeExternalChoice(
                        s,
                        mapOf(
                            "250" to LocalTypeReceive(s, Code250d::class.java, LocalTypeRecursion(tsecureEhlo)),
                            "250d" to LocalTypeReceive(s, Code250::class.java, lAuthC)
                        )
                    )
                )
            ),
            "Quit" to LocalTypeSend(s, UnitClass, LEnd)
        )
    )
    private val lStartTLSC = LocalTypeInternalChoice(
        mapOf(
            "Continue" to LocalTypeSend(
                s,
                UnitClass,
                LocalTypeReceive(
                    s, Code220::class.java,
                    lSecureEhloC
                )
            ),
            "Quit" to LocalTypeSend(s, UnitClass, LEnd)
        )
    )
    private val lEhloC = LocalTypeInternalChoice(
        mapOf(
            "Continue" to LocalTypeSend(
                s, Ehlo::class.java,
                LocalTypeRecursionDefinition(
                    tEhlo,
                    LocalTypeExternalChoice(
                        s,
                        mapOf(
                            "250" to LocalTypeReceive(s, Code250d::class.java, LocalTypeRecursion(tEhlo)),
                            "250d" to LocalTypeReceive(s, Code250::class.java, lStartTLSC)
                        )
                    )
                )
            ),
            "Quit" to LocalTypeSend(s, UnitClass, LEnd)
        )
    )
    private val lC = LocalTypeReceive(
        s, Code220::class.java,
        lEhloC
    )

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
