package bench

import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.endpoint.SKServerSocket
import impl.createSocket
import impl.twoBuyerCallbacksSockets
import impl.twoBuyerFluentSockets
import impl.twoBuyerManualSockets
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import org.openjdk.jmh.annotations.Timeout

@State(Scope.Thread)
open class TwoBuyerManualBenchmark {

    private lateinit var sellerSocketA: ServerSocket
    private lateinit var sellerSocketB: ServerSocket
    private lateinit var clientBSocket: ServerSocket
    private lateinit var selector: SelectorManager

    @Setup
    open fun prepare() {
        selector = SelectorManager(Dispatchers.IO)
        sellerSocketA = createSocket(selector)
        sellerSocketB = createSocket(selector)
        clientBSocket = createSocket(selector)
    }

    @TearDown
    open fun tearDown() {
        sellerSocketA.close()
        sellerSocketB.close()
        clientBSocket.close()
    }

    @Benchmark
    open fun benchTwoBuyerManualSockets() {
        twoBuyerManualSockets(sellerSocketA, sellerSocketB, clientBSocket, selector)
    }

}