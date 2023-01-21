package dsl.examples

import com.github.sessionkotlin.lib.dsl.GlobalProtocol
import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.globalProtocolInternal
import com.github.sessionkotlin.lib.dsl.types.*
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
                    send<Address>(b, seller, "ok")
                    send<Date>(seller, b)
                    send<Date>(b, a, "ok")
                }
                branch {
                    send<Unit>(b, seller, "quit")
                    send<Unit>(b, a, "quit")
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
                            LocalTypeReceive(b, Date::class.java, MsgLabel("ok"), LEnd),
                            LocalTypeReceive(b, UnitClass, MsgLabel("quit"), LEnd),
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
                            LocalTypeReceive(b, Address::class.java, MsgLabel("ok"), LocalTypeSend(b, Date::class.java, LEnd)),
                            LocalTypeReceive(b, UnitClass, MsgLabel("quit"), LEnd),
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
                            Address::class.java, MsgLabel("ok"),
                            LocalTypeReceive(seller, Date::class.java, LocalTypeSend(a, Date::class.java, MsgLabel("ok"), LEnd))
                        ),
                        LocalTypeSend(
                            seller, UnitClass, MsgLabel("quit"),
                            LocalTypeSend(a, UnitClass, MsgLabel("quit"), LEnd)
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
