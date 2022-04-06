package lib.examples

import lib.util.IntClass
import lib.util.StringClass
import lib.util.UnitClass
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.globalProtocolInternal
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class TwoBuyers {

    @Test
    fun main() {
        val a = Role("Client A")
        val b = Role("Client B")
        val seller = Role("Seller")

        val aux = globalProtocolInternal {
            choice(b) {
                case("Ok") {
                    send<Address>(b, seller)
                    send<Date>(seller, b)
                    send<Date>(b, a)
                }
                case("Quit") {
                    send<Unit>(b, seller)
                    send<Unit>(b, a)
                }
            }
        }

        val g = globalProtocolInternal {
            send<String>(a, seller)

            send<Int>(seller, a)
            send<Int>(seller, b)

            send<Int>(a, b)

            exec(aux)
        }

        val lA = LocalTypeSend(
            seller, StringClass,
            LocalTypeReceive(
                seller, IntClass,
                LocalTypeSend(
                    b, IntClass,
                    LocalTypeExternalChoice(
                        b,
                        mapOf(
                            "Ok" to LocalTypeReceive(b, Date::class.java, LEnd),
                            "Quit" to LocalTypeReceive(b, UnitClass, LEnd),
                        )
                    )
                )
            )
        )
        val lS = LocalTypeReceive(
            a, StringClass,
            LocalTypeSend(
                a, IntClass,
                LocalTypeSend(
                    b, IntClass,
                    LocalTypeExternalChoice(
                        b,
                        mapOf(
                            "Ok" to LocalTypeReceive(b, Address::class.java, LocalTypeSend(b, Date::class.java, LEnd)),
                            "Quit" to LocalTypeReceive(b, UnitClass, LEnd),
                        )
                    )
                )
            )
        )
        val lB = LocalTypeReceive(
            seller, IntClass,
            LocalTypeReceive(
                a, IntClass,
                LocalTypeInternalChoice(
                    mapOf(
                        "Ok" to LocalTypeSend(
                            seller,
                            Address::class.java,
                            LocalTypeReceive(seller, Date::class.java, LocalTypeSend(a, Date::class.java, LEnd))
                        ),
                        "Quit" to LocalTypeSend(seller, UnitClass, LocalTypeSend(a, UnitClass, LEnd)),
                    )
                )
            )
        )
        assertEquals(g.project(a), lA)
        assertEquals(g.project(seller), lS)
        assertEquals(g.project(b), lB)
    }

    class Address
}
