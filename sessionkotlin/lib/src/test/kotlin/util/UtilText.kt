package util

import com.github.sessionkotlin.lib.api.exception.RefinementException
import com.github.sessionkotlin.lib.util.assertRefinement
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class UtilText {

    @Test
    fun `test assert refinement false`() {
        assertFailsWith<RefinementException> {
            assertRefinement("", false)
        }
    }

    @Test
    fun `test assert refinement true`() {
        assertRefinement("", true)
    }
}
