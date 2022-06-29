package dsl.examples

import com.github.d_costa.sessionkotlin.dsl.GlobalProtocol
import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocolInternal
import com.github.d_costa.sessionkotlin.dsl.types.*
import dsl.util.IntClass
import dsl.util.StringClass
import dsl.util.UnitClass
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class TwoBuyers {

    @Test
    fun main() {
        val a = SKRole("ClientA")
        val b = SKRole("ClientB")
        val seller = SKRole("Seller")

        val aux: GlobalProtocol = {
            choice(b) {
                branch {
                    send<Address>(b, seller)
                    send<Date>(seller, b)
                    send<Date>(b, a)
                }
                branch {
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

            aux()
        }

        val lA = LocalTypeSend(
            seller, StringClass,
            LocalTypeReceive(
                seller, IntClass,
                LocalTypeSend(
                    b, IntClass,
                    LocalTypeExternalChoice(
                        b,
                        listOf(
                            LocalTypeReceive(b, Date::class.java, LEnd),
                            LocalTypeReceive(b, UnitClass, LEnd),
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
                        listOf(
                            LocalTypeReceive(b, Address::class.java, LocalTypeSend(b, Date::class.java, LEnd)),
                            LocalTypeReceive(b, UnitClass, LEnd),
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
                    listOf(
                        LocalTypeSend(
                            seller,
                            Address::class.java,
                            LocalTypeReceive(seller, Date::class.java, LocalTypeSend(a, Date::class.java, LEnd))
                        ),
                        LocalTypeSend(
                            seller, UnitClass,
                            LocalTypeSend(a, UnitClass, LEnd)
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
