import com.github.d_costa.sessionkotlin.parser.RefinementParser
import com.github.d_costa.sessionkotlin.parser.exception.ParsingException
import com.github.d_costa.sessionkotlin.parser.exception.UnresolvedNameException
import com.github.d_costa.sessionkotlin.parser.grammar
import com.github.d_costa.sessionkotlin.parser.symbols.*
import com.github.d_costa.sessionkotlin.parser.symbols.values.toVal
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyntaxTest {

    @Test
    fun `unresolved name`() {
        val ast = grammar.parseToEnd("a + 5 == 7")
        assertFailsWith<UnresolvedNameException> {
            ast.value(emptyMap())
        }
    }

    @Test
    fun `compatible types 2`() {
        val ast = grammar.parseToEnd("a + b == 0.1")
        assert(ast.value(mapOf("a" to (0.05).toVal(), "b" to 0.05.toVal())))
    }

    @Test
    fun `compatible types 3`() {
        val ast = grammar.parseToEnd("a + b == .1")
        assert(ast.value(mapOf("a" to (0.05).toVal(), "b" to 0.05.toVal())))
    }

    @Test
    fun `test string literal`() {
        val ast = grammar.parseToEnd("a + 5 == 'something5'")
        assertEquals(Eq(Plus(Name("a"), cLong(5)), cString("something5")), ast)
        assert(ast.value(mapOf("a" to ("something").toVal())))
    }

    @Test
    fun `bad syntax`() {
        assertFailsWith<ParsingException> {
            RefinementParser.parseToEnd("5 ==")
        }
    }
}
