import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.david.parser.grammar
import org.david.symbols.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class BooleanConnectorsTest {
    @Test
    fun `test and`() {
        val ast = grammar.parseToEnd("a == 2 && b == 3")
        assertEquals(And(Eq(Name("a"), Const(2)), Eq(Name("b"), Const(3))), ast)

        assert(ast.value(mapOf("a" to 2, "b" to 3)))
        assertFalse(ast.value(mapOf("a" to 2, "b" to 0)))
        assertFalse(ast.value(mapOf("a" to 0, "b" to 3)))
        assertFalse(ast.value(mapOf("a" to 0, "b" to 0)))
    }

    @Test
    fun `test and 2`() {
        val ast = grammar.parseToEnd("a == 2 && b == 3 && c == 4")
        assertEquals(And(And(Eq(Name("a"), Const(2)), Eq(Name("b"), Const(3))), Eq(Name("c"), Const(4))), ast)

        assertFalse(ast.value(mapOf("a" to 0, "b" to 0, "c" to 0)))
        assertFalse(ast.value(mapOf("a" to 0, "b" to 0, "c" to 4)))
        assertFalse(ast.value(mapOf("a" to 0, "b" to 3, "c" to 0)))
        assertFalse(ast.value(mapOf("a" to 0, "b" to 3, "c" to 4)))
        assertFalse(ast.value(mapOf("a" to 2, "b" to 0, "c" to 0)))
        assertFalse(ast.value(mapOf("a" to 2, "b" to 0, "c" to 4)))
        assertFalse(ast.value(mapOf("a" to 2, "b" to 3, "c" to 0)))
        assert(ast.value(mapOf("a" to 2, "b" to 3, "c" to 4)))
    }

    @Test
    fun `test literals`() {
        val ast = grammar.parseToEnd("true")
        assertEquals(True, ast)
        assert(ast.value(emptyMap()))

        val ast2 = grammar.parseToEnd("false")
        assertEquals(False, ast2)
        assertFalse(ast2.value(emptyMap()))
    }

    @Test
    fun `test and associative`() {
        val ast = grammar.parseToEnd("true && false && true")
        assertEquals(And(And(True, False), True), ast)
        assertFalse(ast.value(emptyMap()))

        val ast2 = grammar.parseToEnd("true && (false && true)")
        assertEquals(And(True, And(False, True)), ast2)
        assertFalse(ast2.value(emptyMap()))
    }

    @Test
    fun `test or`() {
        val ast = grammar.parseToEnd("a == 2 || b == 3")
        assertEquals(Or(Eq(Name("a"), Const(2)), Eq(Name("b"), Const(3))), ast)

        assert(ast.value(mapOf("a" to 2, "b" to 3)))
        assert(ast.value(mapOf("a" to 2, "b" to 4)))
        assert(ast.value(mapOf("a" to 4, "b" to 3)))
        assertFalse(ast.value(mapOf("a" to 4, "b" to 4)))
    }

    @Test
    fun `test or 2`() {
        val ast = grammar.parseToEnd("a == 2 || b == 3 || c == 4")
        assertEquals(Or(Or(Eq(Name("a"), Const(2)), Eq(Name("b"), Const(3))), Eq(Name("c"), Const(4))), ast)

        assertFalse(ast.value(mapOf("a" to 0, "b" to 0, "c" to 0)))
        assert(ast.value(mapOf("a" to 0, "b" to 0, "c" to 4)))
        assert(ast.value(mapOf("a" to 0, "b" to 3, "c" to 0)))
        assert(ast.value(mapOf("a" to 0, "b" to 3, "c" to 4)))
        assert(ast.value(mapOf("a" to 2, "b" to 0, "c" to 0)))
        assert(ast.value(mapOf("a" to 2, "b" to 0, "c" to 4)))
        assert(ast.value(mapOf("a" to 2, "b" to 3, "c" to 0)))
        assert(ast.value(mapOf("a" to 2, "b" to 3, "c" to 4)))
    }

    @Test
    fun `test or associative`() {
        val ast = grammar.parseToEnd("true || false || true")
        assertEquals(Or(Or(True, False), True), ast)
        assert(ast.value(emptyMap()))

        val ast2 = grammar.parseToEnd("true || (false || true)")
        assertEquals(Or(True, Or(False, True)), ast2)
        assert(ast2.value(emptyMap()))
    }

    @Test
    fun `test and before or`() {
        val ast = grammar.parseToEnd("true || false && true")
        assertEquals(Or(True, And(False, True)), ast)
        assert(ast.value(emptyMap()))

        val ast2 = grammar.parseToEnd("(true || false) && true")
        assertEquals(And(Or(True, False), True), ast2)
        assert(ast2.value(emptyMap()))
    }

    @Test
    fun `test negation`() {
        val ast = grammar.parseToEnd("!true")
        assertEquals(Not(True), ast)
        assertFalse(ast.value(emptyMap()))

        val ast2 = grammar.parseToEnd("!true || false")
        assertEquals(Or(Not(True), False), ast2)
        assertFalse(ast2.value(emptyMap()))
    }
}
