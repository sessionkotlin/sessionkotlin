package dsl.examples

import dsl.util.IntClass
import dsl.util.StringClass
import dsl.util.UnitClass
import org.david.sessionkotlin_lib.dsl.SKRole
import org.david.sessionkotlin_lib.dsl.globalProtocolInternal
import org.david.sessionkotlin_lib.dsl.types.*
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class TwoBuyers {

    @Test
    fun main() {
        val a = SKRole("Client A")
        val b = SKRole("Client B")
        val seller = SKRole("Seller")

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
                            LocalTypeReceive(seller, Date::class.java, LocalTypeSend(a, Date::class.java, LEnd, "Ok")),
                            "Ok"
                        ),
                        "Quit" to LocalTypeSend(
                            seller, UnitClass,
                            LocalTypeSend(a, UnitClass, LEnd, "Quit"),
                            "Quit"
                        ),
                    )
                )
            )
        )
        assertEquals(lA, g.project(a))
        assertEquals(lS, g.project(seller))
        assertEquals(lB, g.project(b))
    }

    class Address
}
