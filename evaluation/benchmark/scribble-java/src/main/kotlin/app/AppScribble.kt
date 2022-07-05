package app

import adderProtocol
import impl.adder
import impl.twoBuyer
import twoBuyerProtocol
import kotlin.system.exitProcess

fun main(args: Array<String>) {

    val usage = "Usage: ./gradlew scribble-java --args \"protocolName\""
    if (args.size != 1) {
        println(usage)
        exitProcess(1)
    }

    when (val protocolArg = args[0].lowercase()) {
        twoBuyerProtocol -> twoBuyer()
        adderProtocol -> adder()
        else -> println("Protocol not found: $protocolArg")
    }
}
