package bench

import impl.*
import org.openjdk.jmh.annotations.Benchmark

open class ChannelsBenchmark {

    @Benchmark
    open fun benchAdderCallbacksChannels() {
        adderCallbacksChannels()
    }

    @Benchmark
    open fun benchAdderFluentChannels() {
        adderFluentChannels()
    }

    @Benchmark
    open fun benchAdderManualChannels() {
        adderManualChannels()
    }

    @Benchmark
    open fun benchAdderRefinedCallbacksChannels() {
        adderRefinedCallbacksChannels()
    }

    @Benchmark
    open fun benchAdderRefinedFluentChannels() {
        adderRefinedFluentChannels()
    }

    @Benchmark
    open fun benchTwoBuyerCallbacksChannels() {
        twoBuyerCallbacksChannels()
    }

    @Benchmark
    open fun benchTwoBuyerFluentChannels() {
        twoBuyerFluentChannels()
    }

    @Benchmark
    open fun benchTwoBuyerManualChannels() {
        twoBuyerManualChannels()
    }
}
