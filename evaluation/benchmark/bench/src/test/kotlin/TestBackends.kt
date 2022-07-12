import impl.adderFluentSockets
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import impl.adderScribble
import impl.newCustomServerSocket
import impl.twoBuyerScribble
import org.junit.jupiter.api.Test

class TestBackends {

    @Test
    fun testAdderScribble() {
        adderScribble(newCustomServerSocket())
    }

    @Test
    fun testTwoBuyerScribble() {
        twoBuyerScribble(newCustomServerSocket(), newCustomServerSocket(), newCustomServerSocket())
    }

    @Test
    fun testAdderSocketsFluent() {
        adderFluentSockets(SKMPEndpoint.bind())
    }
}