package app

import adderProtocol
import app.impl.adder
import app.impl.twoBuyer
import twoBuyerProtocol
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val channelsKey = "channels"
    val socketsKey = "sockets"

    val usage = "Usage: ./gradlew sessionkotlin-callbacks --args \"[$channelsKey/$socketsKey] protocolName\""
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
