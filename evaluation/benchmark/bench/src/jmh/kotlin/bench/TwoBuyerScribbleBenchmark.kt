package bench

import impl.newCustomServerSocket
import impl.twoBuyerScribble
import org.openjdk.jmh.annotations.*
import java.nio.channels.ServerSocketChannel

@State(Scope.Thread)
open class TwoBuyerScribbleBenchmark {

    private lateinit var sellerSocketA: ServerSocketChannel
    private lateinit var sellerSocketB: ServerSocketChannel
    private lateinit var bSocket: ServerSocketChannel

    @Setup
    open fun prepare() {
        sellerSocketA = newCustomServerSocket()
        sellerSocketB = newCustomServerSocket()
        bSocket = newCustomServerSocket()
    }

    @TearDown
    open fun tearDown() {
        sellerSocketA.close()
        sellerSocketB.close()
        bSocket.close()
    }

    @Benchmark
    open fun benchTwoBuyerScribble() {
        twoBuyerScribble(sellerSocketA, sellerSocketB, bSocket)
    }
}