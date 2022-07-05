package bench

import app.impl.*
import org.openjdk.jmh.annotations.Benchmark

open class ChannelsBenchmark {

    @Benchmark
    open fun benchAdderFluentChannels() {
        adderFluentChannels()
    }

    @Benchmark
    open fun benchAdderCallbacksChannels() {
        adderCallbacksChannels()
    }


    @Benchmark
    open fun benchTwoBuyerFluentChannels() {
        twoBuyerFluentChannels()
    }

    @Benchmark
    open fun benchTwoBuyerCallbacksChannels() {
        twoBuyerCallbacksChannels()
    }


    @Benchmark
    open fun benchChannelsBaseline() {
        // do nothing
    }
}