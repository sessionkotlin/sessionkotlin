import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.david.parser.exception.UnresolvedNameException
import org.david.parser.grammar
import org.david.symbols.*
import org.david.symbols.variable.Variable
import org.david.symbols.variable.toVar
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyntaxTest {
    companion object {
        val variables: List<Variable> = listOf(
            0.toByte().toVar(), 0.toShort().toVar(),
            0.toVar(), 0.toLong().toVar()
        )
    }

    @Test
    fun `unresolved name`() {
        val ast = grammar.parseToEnd("a + 5 == 7")
        assertFailsWith<UnresolvedNameException> {
            ast.value(emptyMap())
        }
    }

    @Test
    fun `unknown number class`() {
        val ast = grammar.parseToEnd("a + b == c")
        class CustomVariable(override val value: String) : Variable(value) {
            override fun compareTo(other: Variable): Int =
                if (other is CustomVariable)
                    value.compareTo(other.value)
                else
                    throw RuntimeException()

            override fun plus(other: Variable): Variable =
                if (other is CustomVariable)
                    CustomVariable("${value}_${other.value}")
                else
                    throw RuntimeException()

            override fun minus(other: Variable) = throw RuntimeException() // not needed
            override fun unaryMinus() = throw RuntimeException() // not needed
        }
        ast.value(
            mapOf(
                "a" to CustomVariable("hello"),
                "b" to CustomVariable("world"),
                "c" to CustomVariable("hello_world")
            )
        )
    }

    @Test
    fun `compatible types 2`() {
        val ast = grammar.parseToEnd("a + b == 0.1")
        assert(ast.value(mapOf("a" to (0.05).toVar(), "b" to 0.05.toVar())))
    }

    @Test
    fun `compatible types 3`() {
        val ast = grammar.parseToEnd("a + b == .1")
        assert(ast.value(mapOf("a" to (0.05).toVar(), "b" to 0.05.toVar())))
    }

    @Test
    fun `test string literal`() {
        val ast = grammar.parseToEnd("a + 5 == 'something5'")
        assertEquals(Eq(Plus(Name("a"), cInt(5)), cString("something5")), ast)
        assert(ast.value(mapOf("a" to ("something").toVar())))
    }
}
