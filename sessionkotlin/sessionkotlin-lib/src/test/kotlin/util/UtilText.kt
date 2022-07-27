package util

import com.github.d_costa.sessionkotlin.api.exception.RefinementException
import com.github.d_costa.sessionkotlin.util.assertRefinement
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
