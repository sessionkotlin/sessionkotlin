package protocol

import org.david.sessionkotlin_lib.api.SKFluent
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.RootEnv
import org.david.sessionkotlin_lib.dsl.globalProtocol

fun main() {
    val a = Role("A")
    val b = Role("B")

    globalProtocol {
        send<Int>(a, b)
        send<String>(b, a)
    }

}
