package app

import adderProtocol
import app.impl.adder
import app.impl.twoBuyer
import channelsKey
import socketsKey
import twoBuyerProtocol
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    val usage = "Usage: ./gradlew sessionkotlin-fluent --args \"$channelsKey|$socketsKey protocolName\""
    if (args.size != 2) {
        println(usage)
        exitProcess(1)
    }

    val backend = args[0]

    when(val protocolArg = args[1].lowercase()) {
        twoBuyerProtocol -> twoBuyer(backend)
        adderProtocol -> adder(backend)
        else -> println("Protocol not found: $protocolArg")
    }
}
