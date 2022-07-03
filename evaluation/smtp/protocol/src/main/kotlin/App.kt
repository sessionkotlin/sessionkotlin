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
                ehlo()
            }
            branch {
                // Transaction failed
                send<C554>(server, client, Code.C554)
            }
        }
    }
}


private val ehlo: GlobalProtocol = {
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
                    mail()
                }
            }
        }
        branch {
            clientQuit()
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
}

private val clientQuit: GlobalProtocol = {
    send<Quit>(client, server, Code.Quit)
    send<C221>(server, client, Code.C221)  // Service closing transmission channel
}
