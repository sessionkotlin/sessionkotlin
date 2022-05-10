import com.github.d_costa.sessionkotlin.parser.grammar
import com.github.d_costa.sessionkotlin.parser.symbols.*
import com.github.d_costa.sessionkotlin.parser.symbols.values.toVal
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ComparisonTest {
    @Test
    fun `test equality`() {
        val ast = grammar.parseToEnd("a == 2")
        assertEquals(Eq(Name("a"), cLong(2)), ast)
        assert(ast.value(mapOf("a" to 2.toVal())))
        assertFalse(ast.value(mapOf("a" to 3.toVal())))

        val ast2 = grammar.parseToEnd("4 == b")
        assertEquals(Eq(cLong(4), Name("b")), ast2)
        assert(ast2.value(mapOf("b" to 4.toVal())))
        assertFalse(ast2.value(mapOf("b" to 3.toVal())))
    }

    @Test
    fun `test inequality`() {
        val ast = grammar.parseToEnd("a != b")
        assertEquals(Neq(Name("a"), Name("b")), ast)
        assertEquals(true, ast.value(mapOf("a" to 2.toVal(), "b" to 4.toVal())))

        val ast2 = grammar.parseToEnd("4 != 4")
        assertEquals(Neq(cLong(4), cLong(4)), ast2)
        assertFalse(ast2.value(emptyMap()))
    }

    @Test
    fun `test lower`() {
        val ast = grammar.parseToEnd("a + 2 < 10")
        assertEquals(Lower(Plus(Name("a"), cLong(2)), cLong(10)), ast)
        assert(ast.value(mapOf("a" to 2.toVal())))

        val ast2 = grammar.parseToEnd("2 <= 0 + 1")
        assertEquals(LowerEq(cLong(2), Plus(cLong(0), cLong(1))), ast2)
        assertFalse(ast2.value(emptyMap()))
    }

    @Test
    fun `test greater`() {
        val ast = grammar.parseToEnd("i - 1 > 0")
        assertEquals(Greater(Minus(Name("i"), cLong(1)), cLong(0)), ast)
        assertFalse(ast.value(mapOf("i" to 1.toVal())))

        val ast2 = grammar.parseToEnd("b >= c")
        assertEquals(GreaterEq(Name("b"), Name("c")), ast2)
        assert(ast2.value(mapOf("b" to 1.toVal(), "c" to 1.toVal())))
    }

    @Test
    fun `test negation`() {
        val ast = grammar.parseToEnd("!a == 2")
        assertEquals(Not(Eq(Name("a"), cLong(2))), ast)
        assertFalse(ast.value(mapOf("a" to 2.toVal())))
        assert(ast.value(mapOf("a" to 3.toVal())))

        val ast2 = grammar.parseToEnd("!4 == b")
        assertEquals(Not(Eq(cLong(4), Name("b"))), ast2)
        assertFalse(ast2.value(mapOf("b" to 4.toVal())))
        assert(ast2.value(mapOf("b" to 3.toVal())))
    }
}
