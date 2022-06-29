import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol
import com.github.d_costa.sessionkotlin.dsl.types.asFormattedString
import messages.*

val client = SKRole("Client")
val server = SKRole("Server")

fun main() {
    // RFC5321
    val g = globalProtocol("SMTP") {
        // TODO 421
        choice(server) {
            branch(Code.C220) {
                // Service ready
                send<C220>(server, client)
                ehlo()
            }
            branch(Code.C554) {
                // Transaction failed
                send<C554>(server, client)
            }
        }
    }
    println(g.project(client).asFormattedString())
}


val ehlo: GlobalProtocol = {
    lateinit var tEhlo: RecursionTag

    choice(client) {
        branch(Code.Ehlo) {
            send<Ehlo>(client, server)
            tEhlo = mu()

            /*
             * EHLO has a multiline response.
             * The last line does not have a hyphen.
             */
            choice(server) {
                branch(Code.C250Hyphen) {
                    send<C250Hyphen>(server, client)
                    goto(tEhlo)
                }
                branch(Code.C250) {
                    send<C250>(server, client)
                    tls()
                }
            }
        }
        branch(Code.Quit) {
            clientQuit()
        }
    }
}

val tls: GlobalProtocol = {
    // https://datatracker.ietf.org/doc/html/rfc2487#section-3
    send<TLS>(client, server)
    send<C220>(server, client)

    // do TLS negotiation here

    choice(client) {
        branch(Code.Ehlo) {
            secureEhlo()
        }
        branch(Code.Quit) {
            // If the SMTP client decides that the level of authentication or
            // privacy is not high enough for it to continue, it SHOULD issue an
            // SMTP QUIT command immediately after the TLS negotiation is complete.
            send<Quit>(client, server)
        }
    }
}

val secureEhlo: GlobalProtocol = {
    lateinit var tSecureEhlo: RecursionTag

    send<Ehlo>(client, server)

    choice(server) {
        branch(Code.C220) {

            tSecureEhlo = mu()
            /*
             * EHLO has a multiline response.
             * The last line does not have a hyphen.
             */
            choice(server) {
                branch(Code.C250Hyphen) {
                    send<C250Hyphen>(server, client)
                    goto(tSecureEhlo)
                }
                branch(Code.C250) {
                    send<C250>(server, client)
                    mail()
                }
            }
        }
        branch(Code.Quit) {
            // If the SMTP server decides that the level of authentication or
            // privacy is not high enough for it to continue, it SHOULD reply to
            // every SMTP command from the client (other than a QUIT command) with
            // the 554 reply code
            send<C554>(server, client)
        }
    }
}

val mail: GlobalProtocol = {

}

val clientQuit: GlobalProtocol = {
    send<Quit>(client, server)
    send<C221>(server, client)  // Service closing transmission channel
}
