import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.david.sessionkotlin.parser.exception.UnresolvedNameException
import org.david.sessionkotlin.parser.grammar
import org.david.sessionkotlin.parser.symbols.*
import org.david.sessionkotlin.parser.symbols.values.Value
import org.david.sessionkotlin.parser.symbols.values.toVal
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyntaxTest {
    companion object {
        val values: List<Value> = listOf(
            0.toByte().toVal(), 0.toShort().toVal(),
            0.toVal(), 0.toLong().toVal()
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
        class CustomValue(override val value: String) : Value(value) {
            override fun compareTo(other: Value): Int =
                if (other is CustomValue)
                    value.compareTo(other.value)
                else
                    throw RuntimeException()

            override fun plus(other: Value): Value =
                if (other is CustomValue)
                    CustomValue("${value}_${other.value}")
                else
                    throw RuntimeException()

            override fun minus(other: Value) = throw RuntimeException() // not needed
            override fun unaryMinus() = throw RuntimeException() // not needed
        }
        ast.value(
            mapOf(
                "a" to CustomValue("hello"),
                "b" to CustomValue("world"),
                "c" to CustomValue("hello_world")
            )
        )
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
        assertEquals(Eq(Plus(Name("a"), cInt(5)), cString("something5")), ast)
        assert(ast.value(mapOf("a" to ("something").toVal())))
    }
}
