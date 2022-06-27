import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.RecursionTag
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol
import messages.C220
import messages.C250
import messages.C250Hyphen
import messages.EHLO

fun main() {

    val client = SKRole("Client")
    val server = SKRole("Server")
    lateinit var tEhlo: RecursionTag
//    lateinit var tsecureEhlo: RecursionTag
//    lateinit var tAuth: RecursionTag
//    lateinit var tMail1: RecursionTag
//    lateinit var tMail2: RecursionTag
//    lateinit var tMail3: RecursionTag

    val ehlo: GlobalProtocol = {
        choice(client) {
            branch("Continue") {
                send<EHLO>(client, server)
                tEhlo = mu()

                /*
                 * EHLO has a multiline response.
                 * The last line does not have a hyphen.
                 */
                choice(server) {
                    branch("250") {
                        send<C250Hyphen>(server, client)
                        goto(tEhlo)
                    }
                    branch("250hyphen") {
                        send<C250>(server, client)
//                        startTLS()
                    }
                }
            }
            branch("Quit") {
                send<Unit>(client, server)
            }
        }
    }

    // RFC5321
    globalProtocol("SMTP") {
        send<C220>(server, client)
        ehlo()
    }


}