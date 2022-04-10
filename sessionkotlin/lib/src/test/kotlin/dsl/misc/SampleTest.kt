package dsl.misc

import org.david.sessionkotlin_lib.dsl.Samples
import org.junit.jupiter.api.Test

class SampleTest {

    @Test
    fun `test send example`() {
        Samples().send()
    }

    @Test
    fun `test send types example`() {
        Samples().sendTypes()
    }

    @Test
    fun `test choice example`() {
        Samples().choice()
    }

    @Test
    fun `test rec example`() {
        Samples().goto()
    }

    @Test
    fun `test exec example`() {
        Samples().exec()
    }
}
