import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import impl.*
import io.ktor.network.selector.*
import kotlinx.coroutines.Dispatchers
import org.junit.jupiter.api.Test

class TestBackends {

    @Test
    fun testAdderFluentSockets() {
        adderFluentSockets(SKMPEndpoint.bind())
    }

    @Test
    fun testAdderManualSockets() {
        val selector = SelectorManager(Dispatchers.IO)
        adderManualSockets(createSocket(selector), selector)
    }

    @Test
    fun testAdderManualChannels() {
        adderManualChannels()
    }

    @Test
    fun testTwoBuyerManualSockets() {
        val selector = SelectorManager(Dispatchers.IO)
        twoBuyerManualSockets(createSocket(selector), createSocket(selector), createSocket(selector), selector)
    }

    @Test
    fun testTwoBuyerManualChannels() {
        twoBuyerManualChannels()
    }
}