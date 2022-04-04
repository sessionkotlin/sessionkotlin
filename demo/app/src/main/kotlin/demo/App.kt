package demo

import org.david.sessionkotlin_lib.api.SKFluent
import org.david.sessionkotlin_lib.dsl.*
import java.io.File

class ClassX(g: RootEnv, r: Role) : SKFluent(g, r)

fun main() {
    val a = Role("A")
    val b = Role("B")

    val g = globalProtocol {
        send<Int>(a, b)
    }
//    System.setProperty("user.dir", "/home/david/code/session-kotlin/demo/app")
//    println(System.getProperty("user.dir"))
    val fluent = ClassX(g, a)

}
