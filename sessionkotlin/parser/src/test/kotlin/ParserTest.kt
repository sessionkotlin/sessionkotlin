import com.github.h0tk3y.betterParse.grammar.parseToEnd
import org.david.grammar.grammar
import org.junit.jupiter.api.Test

class ParserTest {
    companion object;

    @Test
    fun `test number comparison`() {
        val ast = grammar.parseToEnd("a | b & c")
        val ast2 = grammar.parseToEnd("(a | b) & c")
        val ast3 = grammar.parseToEnd("a | (b & c)")
        println(ast)
        println(ast2)
        println(ast3)
    }
}
