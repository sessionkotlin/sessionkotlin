import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol
import messages.*

val client = SKRole("Client")
val server = SKRole("Server")

fun main() {
    // RFC5321
    globalProtocol("SMTP") {
        // TODO 421
        choice(server) {
            branch {
                // Service ready
                send<C220>(server, client, Code.C220)
                ehlo(tls)()
            }
            branch {
                // Transaction failed
                send<C554>(server, client, Code.C554)
            }
        }
    }
}


private fun ehlo(continuation: GlobalProtocol): GlobalProtocol = {
    lateinit var tEhlo: RecursionTag

    choice(client) {
        branch {
            send<SMTPMessage>(client, server, Code.Ehlo)
            tEhlo = mu()

            /*
             * EHLO has a multiline response.
             * The last line does not have a hyphen.
             */
            choice(server) {
                branch {
                    send<C250Hyphen>(server, client, Code.C250Hyphen)
                    goto(tEhlo)
                }
                branch {
                    send<C250>(server, client, Code.C250)
                    continuation()
                }
            }
        }
        branch {
            clientQuit()
        }
    }
}

/**
 * https://datatracker.ietf.org/doc/html/rfc3207
 */
private val tls: GlobalProtocol = {
    choice(client) {
        branch {
            send<StartTLS>(client, server, Code.TLS)
            send<C220>(server, client, Code.C220)
            // Do TLS handshake

            // Secure ehlo
            ehlo(auth)()
        }
        branch {
            clientQuit()
        }
    }
}

/**
 * https://datatracker.ietf.org/doc/html/rfc4954
 */
private val auth: GlobalProtocol = {
    choice(client) {
        branch {
            send<AuthLogin>(client, server, Code.Auth)
            send<C334>(server, client)
            send<AuthUsername>(client, server)
            send<C334>(server, client)
            send<AuthPassword>(client, server)

            choice(server) {
                branch {
                    // Authentication Succeeded
                    send<C235>(server, client, Code.C235)
                    mail()
                }
                branch {
                    // Authentication unsuccessful
                    val t = mu()
                    choice(server) {
                        branch {
                            send<C535>(server, client, Code.C535)
                        }
                        branch {
                            send<C535Hyphen>(server, client, Code.C535Hyphen)
                            goto(t)
                        }
                    }

                }
                branch {
                    // Application-specific password required
                    val t = mu()
                    choice(server) {
                        branch {
                            send<C534>(server, client, Code.C534)
                        }
                        branch {
                            send<C534Hyphen>(server, client, Code.C534Hyphen)
                            goto(t)
                        }
                    }
                }
                branch {
                    // Encryption required for requested authentication mechanism
                    send<C538>(server, client, Code.C538)
                }
                branch {
                    // Requested authentication method is invalid
                    send<C504>(server, client, Code.C504)
                }
                branch {
                    // Invalid arguments
                    send<C501>(server, client, Code.C501)
                }
            }
        }
    }
}

private val mail: GlobalProtocol = {
    send<Mail>(client, server, Code.Mail)

    choice(server) {
        branch {
            // OK
            send<C250>(server, client, Code.C250)
            recipients()
        }
        branch {
            // Requested action not taken: mailbox name not allowed (e.g.,
            // mailbox syntax incorrect)
            send<C553>(server, client, Code.C553)
        }
        branch {
            // Authentication required
            send<C530>(server, client, Code.C530)
        }
    }
}

private val recipients: GlobalProtocol = {
    val tRecipient = mu()

    choice(client) {
        branch {
            send<RCPT>(client, server, Code.RCPT)

            choice(server) {
                branch {
                    // OK
                    send<C250>(server, client, Code.C250)
                    goto(tRecipient)
                }
                branch {
                    branch550()
                }
            }
        }
        branch {
            data()
        }
    }
}

private val branch550: GlobalProtocol = {
    // Requested action not taken: mailbox unavailable (e.g., mailbox
    // not found, no access, or command rejected for policy reasons)
    val t550 = mu()
    choice(server) {
        branch {
            send<C550>(server, client, Code.C550)
        }
        branch {
            send<C550Hyphen>(server, client, Code.C550Hyphen)
            goto(t550)
        }
    }
}
private val data: GlobalProtocol = {
    send<Data>(client, server, Code.Data)
    send<C354>(server, client, Code.C354)

    bodyHeaders()

    val tMailBody = mu()

    choice(client) {
        branch {
            // Add a line
            send<DataLine>(client, server, Code.DataLine)
            goto(tMailBody)
        }
        branch {
            send<DataOver>(client, server, Code.DataOver)

            choice(server) {
                branch {
                    // Ok
                    send<C250>(server, client, Code.C250)
                }
                branch {
                    branch550()
                }
            }
        }
    }
}

private val bodyHeaders: GlobalProtocol = {
    send<MessageIdHeader>(client, server)
    send<FromHeader>(client, server)
    send<ToHeader>(client, server)
    send<SubjectHeader>(client, server)
}

private val clientQuit: GlobalProtocol = {
    send<Quit>(client, server, Code.Quit)
    send<C221>(server, client, Code.C221)  // Service closing transmission channel
}
