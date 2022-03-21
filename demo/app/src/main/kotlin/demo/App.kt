package demo

import org.david.sessionkotlin_lib.annotation.Project
import org.david.sessionkotlin_lib.dsl.*


fun main() {
    val a = Role("A")
    val b = Role("B")
    val c = Role("C")


    val case1 = globalProtocol {
        send<Int>(b, c)
        send<Int>(b, a)
    }

    val case2 = globalProtocol {
        send<String>(b, a)
        send<Int>(b, c)
    }

    val g = globalProtocol {
        choice(b) {

            case("Case 1") {
                exec(case1)
            }
            case("Case 2") {
                exec(case2)
            }
        }
    }

    @Project
    class LocalB(): LocalProtocol(g, b)

}
