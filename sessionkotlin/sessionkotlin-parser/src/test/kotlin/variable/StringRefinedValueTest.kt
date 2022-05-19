package variable

import com.github.d_costa.sessionkotlin.parser.exception.IncompatibleTypesException
import com.github.d_costa.sessionkotlin.parser.grammar
import com.github.d_costa.sessionkotlin.parser.symbols.*
import com.github.d_costa.sessionkotlin.parser.symbols.values.toVal
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StringRefinedValueTest {

    @Test
    fun `test string minus`() {
        val ast = grammar.parseToEnd("a - 'literal' == c")
        assertEquals(Eq(Minus(Name("a"), cString("literal")), Name("c")), ast)
        assertFailsWith<NotImplementedError> {
            ast.value(
                mapOf(
                    "a" to "".toVal(),
                    "b" to "".toVal()
                )
            )
        }
    }

    @Test
    fun `test string unary minus`() {
        val ast = grammar.parseToEnd("-a == b")
        assertEquals(Eq(Neg(Name("a")), Name("b")), ast)
        assertFailsWith<NotImplementedError> {
            ast.value(
                mapOf(
                    "a" to "".toVal(),
                    "b" to "".toVal()
                )
            )
        }
    }

    @Test
    fun `test double plus string`() {
        val ast = grammar.parseToEnd("a + 'v' < b")
        assertEquals(Lower(Plus(Name("a"), cString("v")), Name("b")), ast)
        assert(
            ast.value(
                mapOf(
                    "a" to "ab".toVal(),
                    "b" to "z".toVal(),
                )
            )
        )
    }

    @Test
    fun `test double plus non-string`() {
        val ast = grammar.parseToEnd("a + .3 == b")
        assertEquals(Eq(Plus(Name("a"), cDouble(.3)), Name("b")), ast)
        assert(
            ast.value(
                mapOf(
                    "a" to "ab".toVal(),
                    "b" to "ab0.3".toVal(),
                )
            )
        )
    }

    @Test
    fun `test string compareTo string`() {
        val ast = grammar.parseToEnd("a == 'some'")
        assertEquals(Eq(Name("a"), cString("some")), ast)
        ast.value(mapOf("a" to "".toVal()))
    }

    @Test
    fun `test string compareTo non-string`() {
        val ast = grammar.parseToEnd("a == 2")
        assertEquals(Eq(Name("a"), cLong(2)), ast)
        assertFailsWith<IncompatibleTypesException> {
            ast.value(mapOf("a" to "".toVal()))
        }
    }
}
