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
        assertEquals(Eq(Plus(Name("a"), cLong(5)), cLong(7)), ast)
        assert(ast.value(mapOf("a" to 2.toVal())))
        assertEquals(setOf("a"), ast.names())

        val ast2 = grammar.parseToEnd("4 + 2 == 1 + 5")
        assertEquals(Eq(Plus(cLong(4), cLong(2)), Plus(cLong(1), cLong(5))), ast2)
        assert(ast2.value(emptyMap()))
    }

    @Test
    fun `test plus commutative`() {
        val ast = grammar.parseToEnd("2 + 5 == 5 + 2")
        assertEquals(Eq(Plus(cLong(2), cLong(5)), Plus(cLong(5), cLong(2))), ast)
        assert(ast.value(emptyMap()))
    }

    @Test
    fun `test plus associative`() {
        val ast = grammar.parseToEnd("(2 + 5) + 3 ==  2 + 5 + 3")
        assertEquals(
            Eq(
                Plus(Plus(cLong(2), cLong(5)), cLong(3)),
                Plus(Plus(cLong(2), cLong(5)), cLong(3))
            ),
            ast
        )
        assert(ast.value(emptyMap()))

        val ast2 = grammar.parseToEnd("2 + (5 + 3) ==  2 + 5 + 3")
        assertEquals(
            Eq(
                Plus(cLong(2), Plus(cLong(5), cLong(3))),
                Plus(Plus(cLong(2), cLong(5)), cLong(3))
            ),
            ast2
        )
        assert(ast2.value(emptyMap()))
    }

    @Test
    fun `test minus`() {
        val ast = grammar.parseToEnd("b - 3 == 1")
        assertEquals(Eq(Minus(Name("b"), cLong(3)), cLong(1)), ast)
        assert(ast.value(mapOf("b" to 4.toVal())))
        assertEquals(setOf("b"), ast.names())

        val ast2 = grammar.parseToEnd("4 - 2 != 1 - 2")
        assertEquals(Neq(Minus(cLong(4), cLong(2)), Minus(cLong(1), cLong(2))), ast2)
        assert(ast2.value(emptyMap()))
        assertEquals(emptySet(), ast2.names())
    }

    @Test
    fun `test minus (non) commutative`() {
        val ast = grammar.parseToEnd("2 - 5 != 5 - 2")
        assertEquals(Neq(Minus(cLong(2), cLong(5)), Minus(cLong(5), cLong(2))), ast)
        assert(ast.value(emptyMap()))
        assertEquals(emptySet(), ast.names())
    }

    @Test
    fun `test minus associative`() {
        val ast = grammar.parseToEnd("(2 - 5) - 3 ==  2 - 5 - 3")
        assertEquals(
            Eq(
                Minus(Minus(cLong(2), cLong(5)), cLong(3)),
                Minus(Minus(cLong(2), cLong(5)), cLong(3))
            ),
            ast
        )
        assert(ast.value(emptyMap()))

        val ast2 = grammar.parseToEnd("2 - (5 - 3) ==  2 - 5 - 3")
        assertEquals(
            Eq(
                Minus(cLong(2), Minus(cLong(5), cLong(3))),
                Minus(Minus(cLong(2), cLong(5)), cLong(3))
            ),
            ast2
        )
        assertFalse(ast2.value(emptyMap()))
    }

    @Test
    fun `test unary minus`() {
        val ast = grammar.parseToEnd("-b == -1")
        assertEquals(Eq(Neg(Name("b")), Neg(cLong(1))), ast)
        assert(ast.value(mapOf("b" to 1.toVal())))

        val ast2 = grammar.parseToEnd("1 == -b")
        assertEquals(Eq(cLong(1), Neg(Name("b"))), ast2)
        assert(ast2.value(mapOf("b" to (-1).toVal())))
        assertEquals(setOf("b"), ast2.names())
    }

    @Test
    fun `test double`() {
        val ast = grammar.parseToEnd("a == 0.3")
        assertEquals(Eq(Name("a"), cDouble(.3)), ast)
        assert(ast.value(mapOf("a" to .3.toVal())))
    }

    @Test
    fun `test double 2`() {
        val ast = grammar.parseToEnd("a == .3")
        assertEquals(Eq(Name("a"), cDouble(.3)), ast)
        assert(ast.value(mapOf("a" to .3.toVal())))
    }

    @Test
    fun `test string plus int`() {
        val ast = grammar.parseToEnd("a + b == c")
        assert(ast.value(mapOf("a" to "ab".toVal(), "b" to 3.toVal(), "c" to "ab3".toVal())))
    }

    @Test
    fun `test implication`() {
        val ast = grammar.parseToEnd("a > 0 -> b > 0")
        assertEquals(Impl(Greater(Name("a"), cLong(0)), Greater(Name("b"), cLong(0))), ast)

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
                And(Eq(Name("z"), cLong(1)), Greater(Name("a"), cLong(0))),
                Greater(Name("b"), cLong(0))
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
                Eq(Name("z"), cLong(1)),
                Impl(Greater(Name("a"), cLong(0)), Greater(Name("b"), cLong(0)))
            ),
            ast
        )
    }

    @Test
    fun `test associativity`() {
        val ast = grammar.parseToEnd("a > 0 -> b > 0 -> c > 0")
        assertEquals(
            Impl(
                Greater(Name("a"), cLong(0)),
                Impl(Greater(Name("b"), cLong(0)), Greater(Name("c"), cLong(0)))
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
                Impl(Greater(Name("a"), cLong(0)), Greater(Name("b"), cLong(0))),
                Greater(Name("c"), cLong(0))
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
        val ast = grammar.parseToEnd("a + 3 == c")
        assertEquals(Eq(Plus(Name("a"), cLong(3L)), Name("c")), ast)
        assert(ast.value(mapOf("a" to 1.toVal(), "c" to 4L.toVal())))
    }

    @Test
    fun `test double sum 1`() {
        val ast = grammar.parseToEnd("a + 3.0 == c")
        assertEquals(Eq(Plus(Name("a"), cDouble(3.0)), Name("c")), ast)
        assert(ast.value(mapOf("a" to 1.toVal(), "c" to 4L.toVal())))
    }

    @Test
    fun `test double sum 2`() {
        val ast = grammar.parseToEnd("a + 3.2 >= c")
        assertEquals(GreaterEq(Plus(Name("a"), cDouble(3.2)), Name("c")), ast)
        assert(ast.value(mapOf("a" to 1.toVal(), "c" to 4L.toVal())))
    }

    @Test
    fun `test double sum 3`() {
        val ast = grammar.parseToEnd("a + .2 <= c")
        assertEquals(LowerEq(Plus(Name("a"), cDouble(.2)), Name("c")), ast)
        assert(ast.value(mapOf("a" to 1.toVal(), "c" to 4L.toVal())))
    }

    @Test
    fun `test not`() {
        val ast = grammar.parseToEnd("!a <= c")
        assertEquals(Not(LowerEq(Name("a"), Name("c"))), ast)
        assert(ast.value(mapOf("a" to 3.toVal(), "c" to 2.toVal())))
        assertEquals(setOf("a", "c"), ast.names())
    }

    @Test
    fun `test unary minus precedence`() {
        val ast = grammar.parseToEnd("-1-2 == -3")
        assertEquals(Eq(Minus(Neg(cLong(1)), cLong(2)), Neg(cLong(3))), ast)
        assert(ast.value(emptyMap()))
    }

    @Test
    fun `test unary minus precedence 2`() {
        val ast = grammar.parseToEnd("-a-2 == -3")
        assertEquals(Eq(Minus(Neg(Name("a")), cLong(2)), Neg(cLong(3))), ast)
        assert(ast.value(mapOf("a" to (1).toVal())))
    }

    @Test
    fun `test unary minus precedence 3`() {
        val ast = grammar.parseToEnd("-(a-2) == -3")
        assertEquals(Eq(Neg(Minus(Name("a"), cLong(2))), Neg(cLong(3))), ast)
        assert(ast.value(mapOf("a" to (5).toVal())))
    }

    @Test
    fun `test unary minus 333 3`() {
        val ast = grammar.parseToEnd("a == 1")
//        assertEquals(Eq(Neg(Minus(Name("a"), cLong(2))), Neg(cLong(3))), ast)
        val b : Byte = 1
        assert(ast.value(mapOf("a" to (b).toVal())))
    }
}
