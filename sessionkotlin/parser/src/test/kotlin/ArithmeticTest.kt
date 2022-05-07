import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.david.sessionkotlin.parser.grammar
import org.david.sessionkotlin.parser.symbols.*
import org.david.sessionkotlin.parser.symbols.variable.toVar
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ArithmeticTest {
    @Test
    fun `test plus`() {
        val ast = grammar.parseToEnd("a + 5 == 7")
        assertEquals(Eq(Plus(Name("a"), Const(5.toVar())), cInt(7)), ast)
        assert(ast.value(mapOf("a" to 2.toVar())))

        val ast2 = grammar.parseToEnd("4 + 2 == 1 + 5")
        assertEquals(Eq(Plus(cInt(4), cInt(2)), Plus(cInt(1), cInt(5))), ast2)
        assert(ast2.value(emptyMap()))
    }

    @Test
    fun `test plus commutative`() {
        val ast = grammar.parseToEnd("2 + 5 == 5 + 2")
        assertEquals(Eq(Plus(cInt(2), cInt(5)), Plus(cInt(5), cInt(2))), ast)
        assert(ast.value(emptyMap()))
    }

    @Test
    fun `test plus associative`() {
        val ast = grammar.parseToEnd("(2 + 5) + 3 ==  2 + 5 + 3")
        assertEquals(
            Eq(
                Plus(Plus(cInt(2), cInt(5)), cInt(3)),
                Plus(Plus(cInt(2), cInt(5)), cInt(3))
            ),
            ast
        )
        assert(ast.value(emptyMap()))

        val ast2 = grammar.parseToEnd("2 + (5 + 3) ==  2 + 5 + 3")
        assertEquals(
            Eq(
                Plus(cInt(2), Plus(cInt(5), cInt(3))),
                Plus(Plus(cInt(2), cInt(5)), cInt(3))
            ),
            ast2
        )
        assert(ast2.value(emptyMap()))
    }

    @Test
    fun `test minus`() {
        val ast = grammar.parseToEnd("b - 3 == 1")
        assertEquals(Eq(Minus(Name("b"), cInt(3)), cInt(1)), ast)
        assert(ast.value(mapOf("b" to 4.toVar())))

        val ast2 = grammar.parseToEnd("4 - 2 != 1 - 2")
        assertEquals(Neq(Minus(cInt(4), cInt(2)), Minus(cInt(1), cInt(2))), ast2)
        assert(ast2.value(emptyMap()))
    }

    @Test
    fun `test minus (non) commutative`() {
        val ast = grammar.parseToEnd("2 - 5 != 5 - 2")
        assertEquals(Neq(Minus(cInt(2), cInt(5)), Minus(cInt(5), cInt(2))), ast)
        assert(ast.value(emptyMap()))
    }

    @Test
    fun `test minus associative`() {
        val ast = grammar.parseToEnd("(2 - 5) - 3 ==  2 - 5 - 3")
        assertEquals(
            Eq(
                Minus(Minus(cInt(2), cInt(5)), cInt(3)),
                Minus(Minus(cInt(2), cInt(5)), cInt(3))
            ),
            ast
        )
        assert(ast.value(emptyMap()))

        val ast2 = grammar.parseToEnd("2 - (5 - 3) ==  2 - 5 - 3")
        assertEquals(
            Eq(
                Minus(cInt(2), Minus(cInt(5), cInt(3))),
                Minus(Minus(cInt(2), cInt(5)), cInt(3))
            ),
            ast2
        )
        assertFalse(ast2.value(emptyMap()))
    }

    @Test
    fun `test unary minus`() {
        val ast = grammar.parseToEnd("-b == -1")
        assertEquals(Eq(Neg(Name("b")), Neg(cInt(1))), ast)
        assert(ast.value(mapOf("b" to 1.toVar())))

        val ast2 = grammar.parseToEnd("1 == -b")
        assertEquals(Eq(cInt(1), Neg(Name("b"))), ast2)
        assert(ast2.value(mapOf("b" to (-1).toVar())))
    }

    @Test
    fun `test floating`() {
        val ast = grammar.parseToEnd("a == 0.3f")
        assertEquals(Eq(Name("a"), cFloat(.3f)), ast)
        assert(ast.value(mapOf("a" to .3F.toVar())))
    }

    @Test
    fun `test floating 2`() {
        val ast = grammar.parseToEnd("a == .3F")
        assertEquals(Eq(Name("a"), cFloat(.3F)), ast)
        assert(ast.value(mapOf("a" to .3F.toVar())))
    }

    @Test
    fun `test string plus int`() {
        val ast = grammar.parseToEnd("a + b == c")
        assert(ast.value(mapOf("a" to "ab".toVar(), "b" to 3.toVar(), "c" to "ab3".toVar())))
    }

    @Test
    fun `test double`() {
        val ast = grammar.parseToEnd("a == 3.0")
        assertEquals(Eq(Name("a"), cDouble(3.0)), ast)
        assert(ast.value(mapOf("a" to 3.0.toVar())))
    }

    @Test
    fun `test double 2`() {
        val ast = grammar.parseToEnd("a == .3")
        assertEquals(Eq(Name("a"), cDouble(.3)), ast)
        assert(ast.value(mapOf("a" to .3.toVar())))
    }

    @Test
    fun `test implication`() {
        val ast = grammar.parseToEnd("a > 0 -> b > 0")
        assertEquals(Impl(Greater(Name("a"), cInt(0)), Greater(Name("b"), cInt(0))), ast)

        assert(ast.value(mapOf("a" to 0.toVar(), "b" to 0.toVar())))
        assert(ast.value(mapOf("a" to 0.toVar(), "b" to 1.toVar())))
        assertFalse(ast.value(mapOf("a" to 1.toVar(), "b" to 0.toVar())))
        assert(ast.value(mapOf("a" to 1.toVar(), "b" to 1.toVar())))
    }

    @Test
    fun `test implication 2`() {
        val ast = grammar.parseToEnd("z == 1 && a > 0 -> b > 0")
        assertEquals(
            Impl(
                And(Eq(Name("z"), cInt(1)), Greater(Name("a"), cInt(0))),
                Greater(Name("b"), cInt(0))
            ),
            ast
        )
    }

    @Test
    fun `test implication 3`() {
        val ast = grammar.parseToEnd("z == 1 && (a > 0 -> b > 0)")
        assertEquals(
            And(
                Eq(Name("z"), cInt(1)),
                Impl(Greater(Name("a"), cInt(0)), Greater(Name("b"), cInt(0)))
            ),
            ast
        )
    }

    @Test
    fun `test associativity`() {
        val ast = grammar.parseToEnd("a > 0 -> b > 0 -> c > 0")
        assertEquals(
            Impl(
                Greater(Name("a"), cInt(0)),
                Impl(Greater(Name("b"), cInt(0)), Greater(Name("c"), cInt(0)))
            ),
            ast
        )
        assert(ast.value(mapOf("a" to 0.toVar(), "b" to 0.toVar(), "c" to 0.toVar())))
        assert(ast.value(mapOf("a" to 0.toVar(), "b" to 0.toVar(), "c" to 1.toVar())))
        assert(ast.value(mapOf("a" to 0.toVar(), "b" to 1.toVar(), "c" to 0.toVar())))
        assert(ast.value(mapOf("a" to 0.toVar(), "b" to 1.toVar(), "c" to 1.toVar())))
        assert(ast.value(mapOf("a" to 1.toVar(), "b" to 0.toVar(), "c" to 0.toVar())))
        assert(ast.value(mapOf("a" to 1.toVar(), "b" to 0.toVar(), "c" to 1.toVar())))
        assertFalse(ast.value(mapOf("a" to 1.toVar(), "b" to 1.toVar(), "c" to 0.toVar())))
        assert(ast.value(mapOf("a" to 1.toVar(), "b" to 1.toVar(), "c" to 1.toVar())))
    }

    @Test
    fun `test associativity 2`() {
        val ast = grammar.parseToEnd("(a > 0 -> b > 0) -> c > 0")
        assertEquals(
            Impl(
                Impl(Greater(Name("a"), cInt(0)), Greater(Name("b"), cInt(0))),
                Greater(Name("c"), cInt(0))
            ),
            ast
        )
        assertFalse(ast.value(mapOf("a" to 0.toVar(), "b" to 0.toVar(), "c" to 0.toVar())))
        assert(ast.value(mapOf("a" to 0.toVar(), "b" to 0.toVar(), "c" to 1.toVar())))
        assertFalse(ast.value(mapOf("a" to 0.toVar(), "b" to 1.toVar(), "c" to 0.toVar())))
        assert(ast.value(mapOf("a" to 0.toVar(), "b" to 1.toVar(), "c" to 1.toVar())))
        assert(ast.value(mapOf("a" to 1.toVar(), "b" to 0.toVar(), "c" to 0.toVar())))
        assert(ast.value(mapOf("a" to 1.toVar(), "b" to 0.toVar(), "c" to 1.toVar())))
        assertFalse(ast.value(mapOf("a" to 1.toVar(), "b" to 1.toVar(), "c" to 0.toVar())))
        assert(ast.value(mapOf("a" to 1.toVar(), "b" to 1.toVar(), "c" to 1.toVar())))
    }

    @Test
    fun `test long sum`() {
        val ast = grammar.parseToEnd("a + 3L == c")
        assertEquals(Eq(Plus(Name("a"), cLong(3L)), Name("c")), ast)
        assert(ast.value(mapOf("a" to 1.toVar(), "c" to 4L.toVar())))
    }

    @Test
    fun `test float sum 1`() {
        val ast = grammar.parseToEnd("a + 3F == c")
        assertEquals(Eq(Plus(Name("a"), cFloat(3F)), Name("c")), ast)
        assert(ast.value(mapOf("a" to 1.toVar(), "c" to 4L.toVar())))
    }

    @Test
    fun `test float sum 2`() {
        val ast = grammar.parseToEnd("a + 3.2F >= c")
        assertEquals(GreaterEq(Plus(Name("a"), cFloat(3.2F)), Name("c")), ast)
        assert(ast.value(mapOf("a" to 1.toVar(), "c" to 4L.toVar())))
    }

    @Test
    fun `test float sum 3`() {
        val ast = grammar.parseToEnd("a + .2f <= c")
        assertEquals(LowerEq(Plus(Name("a"), cFloat(.2F)), Name("c")), ast)
        assert(ast.value(mapOf("a" to 1.toVar(), "c" to 4L.toVar())))
    }
}
