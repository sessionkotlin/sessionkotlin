import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.david.parser.grammar
import org.david.symbols.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ArithmeticTest {
    @Test
    fun `test plus`() {
        val ast = grammar.parseToEnd("a + 5 == 7")
        assertEquals(Eq(Plus(Name("a"), Const(5)), Const(7)), ast)
        assert(ast.value(mapOf("a" to 2)))

        val ast2 = grammar.parseToEnd("4 + 2 == 1 + 5")
        assertEquals(Eq(Plus(Const(4), Const(2)), Plus(Const(1), Const(5))), ast2)
        assert(ast2.value(emptyMap()))
    }

    @Test
    fun `test plus commutative`() {
        val ast = grammar.parseToEnd("2 + 5 == 5 + 2")
        assertEquals(Eq(Plus(Const(2), Const(5)), Plus(Const(5), Const(2))), ast)
        assert(ast.value(emptyMap()))
    }

    @Test
    fun `test plus associative`() {
        val ast = grammar.parseToEnd("(2 + 5) + 3 ==  2 + 5 + 3")
        assertEquals(
            Eq(
                Plus(Plus(Const(2), Const(5)), Const(3)),
                Plus(Plus(Const(2), Const(5)), Const(3))
            ),
            ast
        )
        assert(ast.value(emptyMap()))

        val ast2 = grammar.parseToEnd("2 + (5 + 3) ==  2 + 5 + 3")
        assertEquals(
            Eq(
                Plus(Const(2), Plus(Const(5), Const(3))),
                Plus(Plus(Const(2), Const(5)), Const(3))
            ),
            ast2
        )
        assert(ast2.value(emptyMap()))
    }

    @Test
    fun `test minus`() {
        val ast = grammar.parseToEnd("b - 3 == 1")
        assertEquals(Eq(Minus(Name("b"), Const(3)), Const(1)), ast)
        assert(ast.value(mapOf("b" to 4)))

        val ast2 = grammar.parseToEnd("4 - 2 != 1 - 2")
        assertEquals(Neq(Minus(Const(4), Const(2)), Minus(Const(1), Const(2))), ast2)
        assert(ast2.value(emptyMap()))
    }

    @Test
    fun `test minus (non) commutative`() {
        val ast = grammar.parseToEnd("2 - 5 != 5 - 2")
        assertEquals(Neq(Minus(Const(2), Const(5)), Minus(Const(5), Const(2))), ast)
        assert(ast.value(emptyMap()))
    }

    @Test
    fun `test minus associative`() {
        val ast = grammar.parseToEnd("(2 - 5) - 3 ==  2 - 5 - 3")
        assertEquals(
            Eq(
                Minus(Minus(Const(2), Const(5)), Const(3)),
                Minus(Minus(Const(2), Const(5)), Const(3))
            ),
            ast
        )
        assert(ast.value(emptyMap()))

        val ast2 = grammar.parseToEnd("2 - (5 - 3) ==  2 - 5 - 3")
        assertEquals(
            Eq(
                Minus(Const(2), Minus(Const(5), Const(3))),
                Minus(Minus(Const(2), Const(5)), Const(3))
            ),
            ast2
        )
        assertFalse(ast2.value(emptyMap()))
    }

    @Test
    fun `test unary minus`() {
        val ast = grammar.parseToEnd("-b == -1")
        assertEquals(Eq(Neg(Name("b")), Neg(Const(1))), ast)
        assert(ast.value(mapOf("b" to 1)))

        val ast2 = grammar.parseToEnd("1 == -b")
        assertEquals(Eq(Const(1), Neg(Name("b"))), ast2)
        assert(ast2.value(mapOf("b" to -1)))
    }
}
