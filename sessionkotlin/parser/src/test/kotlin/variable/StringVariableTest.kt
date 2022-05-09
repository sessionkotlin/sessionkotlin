package variable

import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.david.sessionkotlin.parser.exception.IncompatibleTypesException
import org.david.sessionkotlin.parser.grammar
import org.david.sessionkotlin.parser.symbols.*
import org.david.sessionkotlin.parser.symbols.variable.toVar
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StringVariableTest {

    @Test
    fun `test string minus`() {
        val ast = grammar.parseToEnd("a - 'literal' == c")
        assertEquals(Eq(Minus(Name("a"), cString("literal")), Name("c")), ast)
        assertFailsWith<NotImplementedError> {
            ast.value(
                mapOf(
                    "a" to "".toVar(),
                    "b" to "".toVar()
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
                    "a" to "".toVar(),
                    "b" to "".toVar()
                )
            )
        }
    }

    @Test
    fun `test float plus string`() {
        val ast = grammar.parseToEnd("a + 'v' < b")
        assertEquals(Lower(Plus(Name("a"), cString("v")), Name("b")), ast)
        assert(
            ast.value(
                mapOf(
                    "a" to "ab".toVar(),
                    "b" to "z".toVar(),
                )
            )
        )
    }

    @Test
    fun `test float plus non-string`() {
        val ast = grammar.parseToEnd("a + .3 == b")
        assertEquals(Eq(Plus(Name("a"), cDouble(.3)), Name("b")), ast)
        assert(
            ast.value(
                mapOf(
                    "a" to "ab".toVar(),
                    "b" to "ab0.3".toVar(),
                )
            )
        )
    }

    @Test
    fun `test string compareTo string`() {
        val ast = grammar.parseToEnd("a == 'some'")
        assertEquals(Eq(Name("a"), cString("some")), ast)
        ast.value(mapOf("a" to "".toVar()))
    }

    @Test
    fun `test string compareTo non-string`() {
        val ast = grammar.parseToEnd("a == 2")
        assertEquals(Eq(Name("a"), cInt(2)), ast)
        assertFailsWith<IncompatibleTypesException> {
            ast.value(mapOf("a" to "".toVar()))
        }
    }
}
