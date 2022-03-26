package tester

import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.junit.jupiter.api.Test

class Test {
    companion object {
        val a = Role("A")
        val b = Role("B")
//        val c = Role("C")
    }

    @Test
    fun `test annotation`() {
        globalProtocol {
            send<Int>(a, b)
        }
    }

    @Test
    fun `basic projection`() {
        globalProtocol {
            send<Int>(a, b)
        }
    }
}