package demo


import org.david.sessionkotlin_lib.api.SKBuffer
import org.david.sessionkotlin_lib.dsl.*
import java.io.File

fun main() {
    val a = Role("A")
    val b = Role("B")

    globalProtocol {
        choice(b) {
            case("Case1") {
                send<Int>(b, a)
            }
            case("Case2") {
                send<Long>(b, a)
            }
        }
    }



}
