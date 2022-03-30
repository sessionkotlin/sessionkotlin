package lib.examples

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test
import java.util.*

class BuyerBrokerSupplier {

    @Test
    fun main() {
        val applicant = Role("Applicant")
        val portal = Role("Application Portal")
        val proc = Role("Processing Department")
        val finance = Role("Finance Department")

        globalProtocol {
            send<Application>(applicant, portal)
            send<Application>(portal, proc)
            send<Boolean>(proc, portal)

            choice(portal) {
                case("Approved") {
                    send<Int>(portal, finance)
                    send<Int>(finance, portal)
                    send<Int>(portal, applicant)
                }
                case("Denied") {
                    send<Unit>(portal, finance)
                    send<Unit>(portal, applicant)
                }

            }
        }


    }

    data class Application(
        val name: String,
        val dateOfBirth: Date,
        val salary: Int,
    )
}