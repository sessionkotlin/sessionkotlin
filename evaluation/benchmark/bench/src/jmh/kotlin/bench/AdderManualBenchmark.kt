package bench

import impl.adderManualSockets
import impl.createSocket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown

@State(Scope.Thread)
open class AdderManualBenchmark {

    private lateinit var serverSocket: ServerSocket
    private lateinit var selector: SelectorManager

    @Setup
    open fun prepare() {
        selector = SelectorManager(Dispatchers.IO)
        serverSocket = createSocket(selector)
    }

    @TearDown
    open fun tearDown() {
        serverSocket.close()
    }

    @Benchmark
    open fun benchAdderManualSockets() {
        adderManualSockets(serverSocket, selector)
    }
}