package tester

import org.david.sessionkotlin_lib.dsl.LocalProtocol
import org.david.sessionkotlin_lib.dsl.Role
import org.david.sessionkotlin_lib.dsl.globalProtocol
import org.david.sessionkotlin_processor.Project
import org.junit.jupiter.api.Test

class Test {
    companion object {
        val a = Role("A")
        val b = Role("B")
//        val c = Role("C")
    }

    @Test
    fun `test annotation`() {
        val g = globalProtocol {
            send<Int>(a, b)
        }

        @Project
        class LocalA : LocalProtocol(g, a)
    }

    @Test
    fun `basic projection`() {
        val g = globalProtocol {
            send<Int>(a, b)
        }
        for (i in g.project(a))
            i.dump(0)
    }
}