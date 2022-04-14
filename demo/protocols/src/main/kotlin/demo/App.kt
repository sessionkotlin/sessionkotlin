package demo

import org.david.sessionkotlin_lib.dsl.*

//
//fun main() {
//    globalProtocol {
//        choice(b) {
//            case("Case1") {
//                send<Int>(b, a)
//                val t = miu("X")
//                choice(a) {
//                    case("OK") {
//                        send<String>(a, b)
//                        goto(t)
//                    }
//                    case("Exit") {
//                        send<Unit>(a, b)
//                    }
//                }
//            }
//            case("Case2") {
//                send<Long>(b, a)
//            }
//        }
//    }
//}

fun main() {
    val a = SKRole("A")
    val b = SKRole("B")

    globalProtocol("Simple") {
        send<Int>(b, a)
        send<Int>(a, b)
    }
}