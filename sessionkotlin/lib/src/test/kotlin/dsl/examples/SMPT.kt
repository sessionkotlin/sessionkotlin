package dsl.examples

import dsl.util.UnitClass
import org.david.sessionkotlin.dsl.RecursionTag
import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.globalProtocolInternal
import org.david.sessionkotlin.dsl.types.*
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
        assertEquals(lS, g.project(s))
        assertEquals(lC, g.project(c))
    }

    private val mail = globalProtocolInternal {
        tMail1 = miu("tMail1")

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
                        tMail2 = miu("tMail2")
                        choice(c) {
                            branch("Recipient") {
                                send<Recipient>(c, s)
                                send<Code250>(s, c)
                                goto(tMail2)
                            }
                            branch("Data") {
                                send<Data>(c, s)
                                send<Code354>(s, c)
                                tMail3 = miu("tMail3")

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
    private val auth = globalProtocolInternal {
        tAuth = miu("tAuth")
        choice(c) {
            branch("Continue") {
                send<Auth>(c, s)
                choice(s) {
                    branch("235") {
                        send<Code235>(s, c)
                        exec(mail)
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
    private val secureEhlo = globalProtocolInternal {
        choice(c) {
            branch("Continue") {
                send<Ehlo>(c, s)
                tsecureEhlo = miu("tsecureEhlo")

                choice(s) {
                    branch("250") {
                        send<Code250d>(s, c)
                        goto(tsecureEhlo)
                    }
                    branch("250d") {
                        send<Code250>(s, c)
                        exec(auth)
                    }
                }
            }
            branch("Quit") {
                send<Unit>(c, s)
            }
        }
    }
    private val startTLS = globalProtocolInternal {
        choice(c) {
            branch("Continue") {
                send<Unit>(c, s)
                send<Code220>(s, c)
                // Do TLS handshake here
                exec(secureEhlo)
            }
            branch("Quit") {
                send<Unit>(c, s)
            }
        }
    }
    private val ehlo = globalProtocolInternal {
        choice(c) {
            branch("Continue") {
                send<Ehlo>(c, s)
                tEhlo = miu("tEhlo")

                choice(s) {
                    branch("250") {
                        send<Code250d>(s, c)
                        goto(tEhlo)
                    }
                    branch("250d") {
                        send<Code250>(s, c)
                        exec(startTLS)
                    }
                }
            }
            branch("Quit") {
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
                            "Quit" to LocalTypeSend(c, Code501::class.java, LocalTypeRecursion(tMail1), "Quit"),
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
                                ),
                                "250"
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
                            "235" to LocalTypeSend(c, Code235::class.java, lMailS, "235"),
                            "535" to LocalTypeSend(c, Code535::class.java, LocalTypeRecursion(tAuth), "535")
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
                            "250" to LocalTypeSend(c, Code250d::class.java, LocalTypeRecursion(tsecureEhlo), "250"),
                            "250d" to LocalTypeSend(c, Code250::class.java, lAuthS, "250d")
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
                            "250" to LocalTypeSend(c, Code250d::class.java, LocalTypeRecursion(tEhlo), "250"),
                            "250d" to LocalTypeSend(c, Code250::class.java, lStartTLSS, "250d")
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
                                                LocalTypeReceive(s, Code250::class.java, LocalTypeRecursion(tMail2)),
                                                "Recipient"
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
                                                                    LocalTypeRecursion(tMail3),
                                                                    "Data"
                                                                ),
                                                                "Subject" to LocalTypeSend(
                                                                    s,
                                                                    Subject::class.java,
                                                                    LocalTypeRecursion(tMail3),
                                                                    "Subject"
                                                                ),
                                                                "End" to LocalTypeSend(
                                                                    s, EndOfData::class.java,
                                                                    LocalTypeReceive(
                                                                        s,
                                                                        Code250::class.java,
                                                                        LocalTypeRecursion(tMail1)
                                                                    ),
                                                                    "End"
                                                                )
                                                            )
                                                        )
                                                    )
                                                ),
                                                "Data"
                                            )
                                        )
                                    )
                                )
                            )
                        )
                    ),
                    "Mail"
                ),
                "Quit" to LocalTypeSend(s, UnitClass, LocalTypeReceive(s, Code221::class.java, LEnd), "Quit")
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
                    ),
                    "Continue"
                ),
                "Quit" to LocalTypeSend(s, UnitClass, LEnd, "Quit")
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
                ),
                "Continue"
            ),
            "Quit" to LocalTypeSend(s, UnitClass, LEnd, "Quit")
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
                ),
                "Continue"
            ),
            "Quit" to LocalTypeSend(s, UnitClass, LEnd, "Quit")
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
                ),
                "Continue"
            ),
            "Quit" to LocalTypeSend(s, UnitClass, LEnd, "Quit")
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