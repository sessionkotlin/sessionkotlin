package variable

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.david.sessionkotlin.parser.exception.IncompatibleTypesException
import org.david.sessionkotlin.parser.grammar
import org.david.sessionkotlin.parser.symbols.*
import org.david.sessionkotlin.parser.symbols.values.toVal
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class LongValueTest {

    companion object {
        val astArith = grammar.parseToEnd("a + b == c - d")
        val astComp = grammar.parseToEnd("a != b")
    }

    @Test
    fun `test eq ast`() {
        assertEquals(Eq(Plus(Name("a"), Name("b")), Minus(Name("c"), Name("d"))), astArith)
    }

    @Test
    fun `test long unary minus`() {
        val ast = grammar.parseToEnd("-a < a")
        assertEquals(Lower(Neg(Name("a")), Name("a")), ast)
        assert(
            ast.value(
                mapOf(
                    "a" to 2L.toVal(),
                )
            )
        )
    }

    @Test
    fun `test long plus, minus Byte`() {
        assert(
            astArith.value(
                mapOf(
                    "a" to 2L.toVal(),
                    "b" to 3.toByte().toVal(),
                    "c" to 6L.toVal(),
                    "d" to 1.toByte().toVal()
                )
            )
        )
    }

    @Test
    fun `test long plus, minus Double`() {
        assert(
            astArith.value(
                mapOf(
                    "a" to 2L.toVal(),
                    "b" to 3.0.toVal(),
                    "c" to 6L.toVal(),
                    "d" to 1.0.toVal()
                )
            )
        )
    }

    @Test
    fun `test long plus, minus Float`() {
        assert(
            astArith.value(
                mapOf(
                    "a" to 2L.toVal(),
                    "b" to 3f.toVal(),
                    "c" to 6L.toVal(),
                    "d" to 1f.toVal()
                )
            )
        )
    }

    @Test
    fun `test long plus, minus Int`() {
        assert(
            astArith.value(
                mapOf(
                    "a" to 2L.toVal(),
                    "b" to 3.toVal(),
                    "c" to 6L.toVal(),
                    "d" to 1.toVal()
                )
            )
        )
    }

    @Test
    fun `test long plus, minus Long`() {
        assert(
            astArith.value(
                mapOf(
                    "a" to 2L.toVal(),
                    "b" to 3L.toVal(),
                    "c" to 6L.toVal(),
                    "d" to 1L.toVal()
                )
            )
        )
    }

    @Test
    fun `test long plus, minus Short`() {
        assert(
            astArith.value(
                mapOf(
                    "a" to 2L.toVal(),
                    "b" to 3.toShort().toVal(),
                    "c" to 6L.toVal(),
                    "d" to 1L.toShort().toVal()
                )
            )
        )
    }

    @Test
    fun `test long compareTo byte`() {
        assert(
            astComp.value(
                mapOf(
                    "a" to 2L.toVal(),
                    "b" to 3.toByte().toVal(),
                )
            )
        )
    }

    @Test
    fun `test long compareTo double`() {
        assert(
            astComp.value(
                mapOf(
                    "a" to 2L.toVal(),
                    "b" to 3.0.toVal(),
                )
            )
        )
    }

    @Test
    fun `test long compareTo float`() {
        assert(
            astComp.value(
                mapOf(
                    "a" to 2L.toVal(),
                    "b" to 3F.toVal(),
                )
            )
        )
    }

    @Test
    fun `test long compareTo int`() {
        assert(
            astComp.value(
                mapOf(
                    "a" to 2L.toVal(),
                    "b" to 3.toVal(),
                )
            )
        )
    }

    @Test
    fun `test long compareTo long`() {
        assert(
            astComp.value(
                mapOf(
                    "a" to 2L.toVal(),
                    "b" to 3L.toVal(),
                )
            )
        )
    }

    @Test
    fun `test long compareTo short`() {
        assert(
            astComp.value(
                mapOf(
                    "a" to 2L.toVal(),
                    "b" to 3.toShort().toVal(),
                )
            )
        )
    }

    @Test
    fun `test long compareTo string`() {
        assertFailsWith<IncompatibleTypesException> {
            assert(
                astComp.value(
                    mapOf(
                        "a" to 2L.toVal(),
                        "b" to "something".toVal(),
                    )
                )
            )
        }
    }

    @Test
    fun `test long plus string`() {
        val ast = grammar.parseToEnd("a + b == 3.0")
        assertFailsWith<IncompatibleTypesException> {
            assert(
                ast.value(
                    mapOf(
                        "a" to 2L.toVal(),
                        "b" to "something".toVal(),
                    )
                )
            )
        }
    }

    @Test
    fun `test long minus string`() {
        val ast = grammar.parseToEnd("a - b == 3.0")
        assertFailsWith<IncompatibleTypesException> {
            assert(
                ast.value(
                    mapOf(
                        "a" to 2L.toVal(),
                        "b" to "something".toVal(),
                    )
                )
            )
        }
    }
}
