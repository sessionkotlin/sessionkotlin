package examples

import dsl.Role
import dsl.globalProtocol
import org.junit.jupiter.api.Test
import java.util.*

class Booking {

    @Test
    fun main() {
        val client = Role("Client")
        val agency = Role("Agency")
        val company = Role("Some company")

        globalProtocol {
            choice(client) {
                case("Book") {
                    send<String>(client, agency)
                    send<Int>(agency, client)
                    send<Unit>(agency, company)  // dummy message
                    rec()
                }
                case("Terminate") {
                    choice(client) {
                        case("Confirm") {
                            send<Unit>(client, agency)
                            send<Unit>(agency, company)
                            send<PaymentInfo>(client, company)
                            send<Unit>(company, client)
                        }
                        case("Cancel") {
                            send<Unit>(client, agency)
                            send<Unit>(agency, company)
                        }
                    }
                }
            }
        }

    }

    class PaymentInfo(
        val creditCard: Long,
        val expiration: Date,
    )
}