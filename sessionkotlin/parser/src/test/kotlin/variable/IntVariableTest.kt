package variable

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.david.sessionkotlin.parser.exception.IncompatibleTypesException
import org.david.sessionkotlin.parser.grammar
import org.david.sessionkotlin.parser.symbols.*
import org.david.sessionkotlin.parser.symbols.variable.toVar
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class IntVariableTest {

    companion object {
        val astArith = grammar.parseToEnd("a + b == c - d")
        val astComp = grammar.parseToEnd("a != b")
    }

    @Test
    fun `test eq ast`() {
        assertEquals(Eq(Plus(Name("a"), Name("b")), Minus(Name("c"), Name("d"))), astArith)
    }

    @Test
    fun `test int unary minus`() {
        val ast = grammar.parseToEnd("-a < a")
        assertEquals(Lower(Neg(Name("a")), Name("a")), ast)
        assert(
            ast.value(
                mapOf(
                    "a" to 2.toVar(),
                )
            )
        )
    }

    @Test
    fun `test int plus, minus Byte`() {
        assert(
            astArith.value(
                mapOf(
                    "a" to 2.toVar(),
                    "b" to 3.toByte().toVar(),
                    "c" to 6.toVar(),
                    "d" to 1.toByte().toVar()
                )
            )
        )
    }

    @Test
    fun `test int plus, minus Double`() {
        assert(
            astArith.value(
                mapOf(
                    "a" to 2.toVar(),
                    "b" to 3.0.toVar(),
                    "c" to 6.toVar(),
                    "d" to 1.0.toVar()
                )
            )
        )
    }

    @Test
    fun `test int plus, minus Float`() {
        assert(
            astArith.value(
                mapOf(
                    "a" to 2.toVar(),
                    "b" to 3f.toVar(),
                    "c" to 6.toVar(),
                    "d" to 1f.toVar()
                )
            )
        )
    }

    @Test
    fun `test int plus, minus Int`() {
        assert(
            astArith.value(
                mapOf(
                    "a" to 2.toVar(),
                    "b" to 3.toVar(),
                    "c" to 6.toVar(),
                    "d" to 1.toVar()
                )
            )
        )
    }

    @Test
    fun `test int plus, minus Long`() {
        assert(
            astArith.value(
                mapOf(
                    "a" to 2.toVar(),
                    "b" to 3L.toVar(),
                    "c" to 6.toVar(),
                    "d" to 1L.toVar()
                )
            )
        )
    }

    @Test
    fun `test int plus, minus Short`() {
        assert(
            astArith.value(
                mapOf(
                    "a" to 2.toVar(),
                    "b" to 3.toShort().toVar(),
                    "c" to 6.toVar(),
                    "d" to 1L.toShort().toVar()
                )
            )
        )
    }

    @Test
    fun `test int compareTo byte`() {
        assert(
            astComp.value(
                mapOf(
                    "a" to 2.toVar(),
                    "b" to 3.toByte().toVar(),
                )
            )
        )
    }

    @Test
    fun `test int compareTo double`() {
        assert(
            astComp.value(
                mapOf(
                    "a" to 2.toVar(),
                    "b" to 3.0.toVar(),
                )
            )
        )
    }

    @Test
    fun `test int compareTo float`() {
        assert(
            astComp.value(
                mapOf(
                    "a" to 2.toVar(),
                    "b" to 3F.toVar(),
                )
            )
        )
    }

    @Test
    fun `test int compareTo int`() {
        assert(
            astComp.value(
                mapOf(
                    "a" to 2.toVar(),
                    "b" to 3.toVar(),
                )
            )
        )
    }

    @Test
    fun `test int compareTo long`() {
        assert(
            astComp.value(
                mapOf(
                    "a" to 2.toVar(),
                    "b" to 3L.toVar(),
                )
            )
        )
    }

    @Test
    fun `test int compareTo short`() {
        assert(
            astComp.value(
                mapOf(
                    "a" to 2.toVar(),
                    "b" to 3.toShort().toVar(),
                )
            )
        )
    }

    @Test
    fun `test int compareTo string`() {
        assertFailsWith<IncompatibleTypesException> {
            assert(
                astComp.value(
                    mapOf(
                        "a" to 2.toVar(),
                        "b" to "something".toVar(),
                    )
                )
            )
        }
    }

    @Test
    fun `test int plus string`() {
        val ast = grammar.parseToEnd("a + b == 3.0")
        assertFailsWith<IncompatibleTypesException> {
            assert(
                ast.value(
                    mapOf(
                        "a" to 2.toVar(),
                        "b" to "something".toVar(),
                    )
                )
            )
        }
    }

    @Test
    fun `test int minus string`() {
        val ast = grammar.parseToEnd("a - b == 3.0")
        assertFailsWith<IncompatibleTypesException> {
            assert(
                ast.value(
                    mapOf(
                        "a" to 2.toVar(),
                        "b" to "something".toVar(),
                    )
                )
            )
        }
    }
}
