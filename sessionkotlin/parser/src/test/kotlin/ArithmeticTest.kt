import com.github.d_costa.sessionkotlin.parser.grammar
import com.github.d_costa.sessionkotlin.parser.symbols.*
import com.github.d_costa.sessionkotlin.parser.symbols.values.toVal
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class ArithmeticTest {
    @Test
    fun `test plus`() {
        val ast = grammar.parseToEnd("a + 5 == 7")
        assertEquals(Eq(Plus(Name("a"), Const(5.toVal())), cInt(7)), ast)
        assert(ast.value(mapOf("a" to 2.toVal())))
        assertEquals(setOf("a"), ast.names())

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
        assert(ast.value(mapOf("b" to 4.toVal())))
        assertEquals(setOf("b"), ast.names())

        val ast2 = grammar.parseToEnd("4 - 2 != 1 - 2")
        assertEquals(Neq(Minus(cInt(4), cInt(2)), Minus(cInt(1), cInt(2))), ast2)
        assert(ast2.value(emptyMap()))
        assertEquals(emptySet(), ast2.names())
    }

    @Test
    fun `test minus (non) commutative`() {
        val ast = grammar.parseToEnd("2 - 5 != 5 - 2")
        assertEquals(Neq(Minus(cInt(2), cInt(5)), Minus(cInt(5), cInt(2))), ast)
        assert(ast.value(emptyMap()))
        assertEquals(emptySet(), ast.names())
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
        assert(ast.value(mapOf("b" to 1.toVal())))

        val ast2 = grammar.parseToEnd("1 == -b")
        assertEquals(Eq(cInt(1), Neg(Name("b"))), ast2)
        assert(ast2.value(mapOf("b" to (-1).toVal())))
        assertEquals(setOf("b"), ast2.names())
    }

    @Test
    fun `test floating`() {
        val ast = grammar.parseToEnd("a == 0.3f")
        assertEquals(Eq(Name("a"), cFloat(.3f)), ast)
        assert(ast.value(mapOf("a" to .3F.toVal())))
    }

    @Test
    fun `test floating 2`() {
        val ast = grammar.parseToEnd("a == .3F")
        assertEquals(Eq(Name("a"), cFloat(.3F)), ast)
        assert(ast.value(mapOf("a" to .3F.toVal())))
    }

    @Test
    fun `test string plus int`() {
        val ast = grammar.parseToEnd("a + b == c")
        assert(ast.value(mapOf("a" to "ab".toVal(), "b" to 3.toVal(), "c" to "ab3".toVal())))
    }

    @Test
    fun `test double`() {
        val ast = grammar.parseToEnd("a == 3.0")
        assertEquals(Eq(Name("a"), cDouble(3.0)), ast)
        assert(ast.value(mapOf("a" to 3.0.toVal())))
    }

    @Test
    fun `test double 2`() {
        val ast = grammar.parseToEnd("a == .3")
        assertEquals(Eq(Name("a"), cDouble(.3)), ast)
        assert(ast.value(mapOf("a" to .3.toVal())))
    }

    @Test
    fun `test implication`() {
        val ast = grammar.parseToEnd("a > 0 -> b > 0")
        assertEquals(Impl(Greater(Name("a"), cInt(0)), Greater(Name("b"), cInt(0))), ast)

        assert(ast.value(mapOf("a" to 0.toVal(), "b" to 0.toVal())))
        assert(ast.value(mapOf("a" to 0.toVal(), "b" to 1.toVal())))
        assertFalse(ast.value(mapOf("a" to 1.toVal(), "b" to 0.toVal())))
        assert(ast.value(mapOf("a" to 1.toVal(), "b" to 1.toVal())))
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
        assertEquals(setOf("z", "a", "b"), ast.names())
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
        assert(ast.value(mapOf("a" to 0.toVal(), "b" to 0.toVal(), "c" to 0.toVal())))
        assert(ast.value(mapOf("a" to 0.toVal(), "b" to 0.toVal(), "c" to 1.toVal())))
        assert(ast.value(mapOf("a" to 0.toVal(), "b" to 1.toVal(), "c" to 0.toVal())))
        assert(ast.value(mapOf("a" to 0.toVal(), "b" to 1.toVal(), "c" to 1.toVal())))
        assert(ast.value(mapOf("a" to 1.toVal(), "b" to 0.toVal(), "c" to 0.toVal())))
        assert(ast.value(mapOf("a" to 1.toVal(), "b" to 0.toVal(), "c" to 1.toVal())))
        assertFalse(ast.value(mapOf("a" to 1.toVal(), "b" to 1.toVal(), "c" to 0.toVal())))
        assert(ast.value(mapOf("a" to 1.toVal(), "b" to 1.toVal(), "c" to 1.toVal())))
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
        assertFalse(ast.value(mapOf("a" to 0.toVal(), "b" to 0.toVal(), "c" to 0.toVal())))
        assert(ast.value(mapOf("a" to 0.toVal(), "b" to 0.toVal(), "c" to 1.toVal())))
        assertFalse(ast.value(mapOf("a" to 0.toVal(), "b" to 1.toVal(), "c" to 0.toVal())))
        assert(ast.value(mapOf("a" to 0.toVal(), "b" to 1.toVal(), "c" to 1.toVal())))
        assert(ast.value(mapOf("a" to 1.toVal(), "b" to 0.toVal(), "c" to 0.toVal())))
        assert(ast.value(mapOf("a" to 1.toVal(), "b" to 0.toVal(), "c" to 1.toVal())))
        assertFalse(ast.value(mapOf("a" to 1.toVal(), "b" to 1.toVal(), "c" to 0.toVal())))
        assert(ast.value(mapOf("a" to 1.toVal(), "b" to 1.toVal(), "c" to 1.toVal())))
    }

    @Test
    fun `test long sum`() {
        val ast = grammar.parseToEnd("a + 3L == c")
        assertEquals(Eq(Plus(Name("a"), cLong(3L)), Name("c")), ast)
        assert(ast.value(mapOf("a" to 1.toVal(), "c" to 4L.toVal())))
    }

    @Test
    fun `test float sum 1`() {
        val ast = grammar.parseToEnd("a + 3F == c")
        assertEquals(Eq(Plus(Name("a"), cFloat(3F)), Name("c")), ast)
        assert(ast.value(mapOf("a" to 1.toVal(), "c" to 4L.toVal())))
    }

    @Test
    fun `test float sum 2`() {
        val ast = grammar.parseToEnd("a + 3.2F >= c")
        assertEquals(GreaterEq(Plus(Name("a"), cFloat(3.2F)), Name("c")), ast)
        assert(ast.value(mapOf("a" to 1.toVal(), "c" to 4L.toVal())))
    }

    @Test
    fun `test float sum 3`() {
        val ast = grammar.parseToEnd("a + .2f <= c")
        assertEquals(LowerEq(Plus(Name("a"), cFloat(.2F)), Name("c")), ast)
        assert(ast.value(mapOf("a" to 1.toVal(), "c" to 4L.toVal())))
    }

    @Test
    fun `test not`() {
        val ast = grammar.parseToEnd("!a <= c")
        assertEquals(Not(LowerEq(Name("a"), Name("c"))), ast)
        assert(ast.value(mapOf("a" to 3.toVal(), "c" to 2.toVal())))
        assertEquals(setOf("a", "c"), ast.names())
    }
}
