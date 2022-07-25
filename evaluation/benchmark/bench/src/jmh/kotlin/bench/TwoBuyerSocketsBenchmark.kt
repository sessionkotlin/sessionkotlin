package bench

import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.endpoint.SKServerSocket
import impl.twoBuyerCallbacksSockets
import impl.twoBuyerFluentSockets
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown

@State(Scope.Thread)
open class TwoBuyerSocketsBenchmark {

    private lateinit var sellerSocket: SKServerSocket
    private lateinit var clientBSocket: SKServerSocket

    @Setup
    open fun prepare() {
        sellerSocket = SKMPEndpoint.bind(0)
        clientBSocket = SKMPEndpoint.bind(0)
    }

    @TearDown
    open fun tearDown() {
        sellerSocket.close()
        clientBSocket.close()
    }

    @Benchmark
    open fun benchTwoBuyerCallbacksSockets() {
        twoBuyerCallbacksSockets(sellerSocket, clientBSocket)
    }

    @Benchmark
    open fun benchTwoBuyerFluentSockets() {
        twoBuyerFluentSockets(sellerSocket, clientBSocket)
    }
}