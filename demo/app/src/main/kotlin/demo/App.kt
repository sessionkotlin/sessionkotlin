package demo



import Proto1_A_1
import Proto1_A_2_Case1Interface
import Proto1_A_9_Case2Interface
import org.david.sessionkotlin_lib.api.SKBuffer
import org.david.sessionkotlin_lib.dsl.*
import java.io.File

fun main() {
    val a = Role("A")
    val b = Role("B")

    globalProtocol {
        choice(b) {
            case("Case 1") {
                send<Int>(b, a)
                choice(a) {
                    case("OK") {
                        send<String>(a, b)
                    }
                    case("Exit") {
                        send<Unit>(a, b)
                    }
                }
            }
            case("Case 2") {
                send<Long>(b, a)
            }
        }
    }

    val b1 = Proto1_A_1()
        .branch()

    when(b1) {
        is Proto1_A_2_Case1Interface ->
            b1.receiveFromB(SKBuffer())
                .branchOK()
                .sendToB("")
        is Proto1_A_9_Case2Interface ->
            b1.receiveFromB(SKBuffer())
    }
}
