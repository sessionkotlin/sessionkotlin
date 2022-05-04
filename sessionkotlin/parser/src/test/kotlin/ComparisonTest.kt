import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.david.grammar.grammar
import org.david.symbols.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ComparisonTest {
    @Test
    fun `test equality`() {
        val ast = grammar.parseToEnd("a == 2")
        assertEquals(Eq(Name("a"), Const(2)), ast)
        assert(ast.value(mapOf("a" to 2)))
        assertFalse(ast.value(mapOf("a" to 3)))

        val ast2 = grammar.parseToEnd("4 == b")
        assertEquals(Eq(Const(4), Name("b")), ast2)
        assert(ast2.value(mapOf("b" to 4)))
        assertFalse(ast2.value(mapOf("b" to 3)))
    }

    @Test
    fun `test inequality`() {
        val ast = grammar.parseToEnd("a != b")
        assertEquals(Neq(Name("a"), Name("b")), ast)
        assertEquals(true, ast.value(mapOf("a" to 2, "b" to 4)))

        val ast2 = grammar.parseToEnd("4 != 4")
        assertEquals(Neq(Const(4), Const(4)), ast2)
        assertFalse(ast2.value(emptyMap()))
    }

    @Test
    fun `test lower`() {
        val ast = grammar.parseToEnd("a + 2 < 10")
        assertEquals(Lower(Plus(Name("a"), Const(2)), Const(10)), ast)
        assert(ast.value(mapOf("a" to 2)))

        val ast2 = grammar.parseToEnd("2 <= 0 + 1")
        assertEquals(LowerEq(Const(2), Plus(Const(0), Const(1))), ast2)
        assertFalse(ast2.value(emptyMap()))
    }

    @Test
    fun `test greater`() {
        val ast = grammar.parseToEnd("i - 1 > 0")
        assertEquals(Greater(Minus(Name("i"), Const(1)), Const(0)), ast)
        assertFalse(ast.value(mapOf("i" to 1)))

        val ast2 = grammar.parseToEnd("b >= c")
        assertEquals(GreaterEq(Name("b"), Name("c")), ast2)
        assert(ast2.value(mapOf("b" to 1, "c" to 1)))
    }

    @Test
    fun `test negation`() {
        val ast = grammar.parseToEnd("!a == 2")
        assertEquals(Not(Eq(Name("a"), Const(2))), ast)
        assertFalse(ast.value(mapOf("a" to 2)))
        assert(ast.value(mapOf("a" to 3)))

        val ast2 = grammar.parseToEnd("!4 == b")
        assertEquals(Not(Eq(Const(4), Name("b"))), ast2)
        assertFalse(ast2.value(mapOf("b" to 4)))
        assert(ast2.value(mapOf("b" to 3)))
    }
}
