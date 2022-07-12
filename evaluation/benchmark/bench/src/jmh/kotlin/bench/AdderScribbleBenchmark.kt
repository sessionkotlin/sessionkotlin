package bench

import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.endpoint.SKServerSocket
import impl.adderScribble
import impl.newCustomServerSocket
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.Setup
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.TearDown
import java.nio.channels.ServerSocketChannel

@State(Scope.Thread)
open class AdderScribbleBenchmark {

    private lateinit var serverSocket: ServerSocketChannel

    @Setup
    open fun prepare() {
        serverSocket = newCustomServerSocket()
    }

    @TearDown
    open fun tearDown() {
        serverSocket.close()
    }

    @Benchmark
    open fun benchAdderScribble() {
        adderScribble(serverSocket)
    }
}