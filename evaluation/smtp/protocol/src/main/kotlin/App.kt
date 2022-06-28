import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol
import messages.*

fun main() {

    val client = SKRole("Client")
    val server = SKRole("Server")
    lateinit var tEhlo: RecursionTag
//    lateinit var tsecureEhlo: RecursionTag
//    lateinit var tAuth: RecursionTag
//    lateinit var tMail1: RecursionTag
//    lateinit var tMail2: RecursionTag
//    lateinit var tMail3: RecursionTag

    val quit: GlobalProtocol = {
        send<Quit>(client, server)
        send<C221>(server, client)  // Service closing transmission channel
    }

    val ehlo: GlobalProtocol = {
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
//                        startTLS()
                    }
                }
            }
            branch(Code.Quit) {
                quit()
            }
        }
    }

    // RFC5321
    globalProtocol("SMTP") {
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


}