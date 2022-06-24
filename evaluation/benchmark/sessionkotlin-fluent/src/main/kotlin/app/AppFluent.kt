package app

import channelsKey
import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import guessing_game.Magician
import guessing_game.User
import guessing_game.fluent.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lib.guess
import lib.randomLong
import maxValue
import minValue
import port
import socketsKey
import kotlin.system.exitProcess


fun main(args: Array<String>) {
    val usage = "Usage: ./gradlew sessionkotlin-fluent --args [$channelsKey/$socketsKey]"
    if (args.size != 1) {
        println(usage)
        exitProcess(1)
    }
    val secretNumber = randomLong()
    when (args[0]) {
        channelsKey -> channels(secretNumber)
        socketsKey -> sockets(secretNumber)
        else -> println("Unsupported API. $usage")
    }
}

fun channels(secretNumber: Long) {
    val chan = SKChannel()
    runBlocking {
        launch {
            // Magician
            SKMPEndpoint().use { e ->
                e.connect(User, chan)
                magicianProtocol(e, secretNumber)
            }
        }
        launch {
            // Magician
            SKMPEndpoint().use { e ->
                e.connect(Magician, chan)
                userProtocol(e)
            }
        }
    }
}

fun sockets(secretNumber: Long) {
    runBlocking {
        launch {
            // Magician
            SKMPEndpoint().use { e ->
                e.accept(User, port)
                magicianProtocol(e, secretNumber)
            }
        }
        launch {
            // User
            SKMPEndpoint().use { e ->
                e.request(Magician, "localhost", port)
                userProtocol(e)
            }
        }
    }
}

suspend fun magicianProtocol(e: SKMPEndpoint, secretNumber: Long) {
    val buf = SKBuffer<Long>()
    GuessingGameMagician1(e)
        .sendToUser(minValue)
        .sendToUser(maxValue)
        .let { initialState3 ->
            var state3 = initialState3
            do {
                var found = false
                val state4 = state3.receiveFromUser(buf)
                if (buf.value > secretNumber)
                    state3 = state4
                        .branchLower()
                        .sendToUser()
                else if (buf.value < secretNumber)
                    state3 = state4
                        .branchHigher()
                        .sendToUser()
                else {
                    state4
                        .branchCorrect()
                        .sendToUser()
                    found = true
                }

            } while (!found)
        }
}

suspend fun userProtocol(e: SKMPEndpoint) {
    val buf = SKBuffer<Long>()
    var currMin: Long
    var currMax: Long
    var c = 0
    GuessingGameUser1(e)
        .receiveFromMagician(buf).also { currMin = buf.value }
        .receiveFromMagician(buf).also { currMax = buf.value }
        .let { initialState3 ->
            var state3 = initialState3
            do {
                c += 1
                var found = false
                val guess = guess(currMin, currMax)
                when (val b = state3.sendToMagician(guess).branch()) {
                    is GuessingGameUser5_Higher -> state3 = b.receiveFromMagician()
                        .also { currMin = guess + 1 }
                    is GuessingGameUser7_Lower -> state3 = b.receiveFromMagician()
                        .also { currMax = guess - 1 }
                    is GuessingGameUser9_Correct -> {
                        found = true
                    }
                }
            } while (!found)
        }
}
