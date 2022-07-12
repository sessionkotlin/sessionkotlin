package bench

import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.endpoint.SKServerSocket
import impl.adderCallbacksSockets
import impl.adderFluentSockets
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown

@State(Scope.Thread)
open class AdderSocketsBenchmark {

    private lateinit var serverSocket: SKServerSocket

    @Setup
    open fun prepare() {
        serverSocket = SKMPEndpoint.bind(0)
    }

    @TearDown
    open fun tearDown() {
        serverSocket.close()
    }

    @Benchmark
    open fun benchAdderFluentSockets() {
        adderFluentSockets(serverSocket)
    }

    @Benchmark
    open fun benchAdderCallbacksSockets() {
        adderCallbacksSockets(serverSocket)
    }
}