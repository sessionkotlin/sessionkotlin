package app

import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import guessing_game.Magician
import guessing_game.User
import guessing_game.callback.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lib.guess
import lib.randomLong
import maxValue
import minValue
import port
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val channelsKey = "channels"
    val socketsKey = "sockets"

    val usage = "Usage: ./gradlew sessionkotlin-callbacks --args [$channelsKey/$socketsKey]"
    if (args.size != 1) {
        println(usage)
        exitProcess(1)
    }
    val secretNumber = randomLong()
    when(args[0]) {
        channelsKey -> channels(secretNumber)
        socketsKey -> sockets(secretNumber)
        else -> println("Unsupported API. $usage")
    }
}

fun channels(secretNumber: Long) {
    val chan = SKChannel(Magician, User)
    runBlocking {
        launch {
            // Magician
            GuessingGameCallbackEndpointMagician(magicianProtocol(secretNumber)).use {
                it.connect(User, chan)
                it.start()
            }

        }
        launch {
            // User
            GuessingGameCallbackEndpointUser(userProtocol()).use {
                it.connect(Magician, chan)
                it.start()
            }
        }
    }
}

fun sockets(secretNumber: Long) {
    runBlocking {
        launch {
            // Magician
            GuessingGameCallbackEndpointMagician(magicianProtocol(secretNumber)).use {
                it.accept(User, port)
                it.start()
            }

        }
        launch {
            // User
            GuessingGameCallbackEndpointUser(userProtocol()).use {
                it.request(Magician, "localhost", port)
                it.start()
            }
        }
    }
}

fun magicianProtocol(secretNumber: Long): GuessingGameCallbacksMagician {
    return object : GuessingGameCallbacksMagician {
        lateinit var response: Choice4

        override fun onReceiveGuessFromUser(v: Long) {
            response = if (v < secretNumber) {
                Choice4.Choice4_Higher
            } else if (v > secretNumber) {
                Choice4.Choice4_Lower
            } else {
                Choice4.Choice4_Correct
            }
        }
        override fun onSendMaxToUser(): Long = maxValue
        override fun onSendMinToUser(): Long = minValue

        override fun onChoose4(): Choice4 = response

        override fun onSendDummyHigherToUser() { }
        override fun onSendDummyLowerToUser() { }
        override fun onSendDummyCorrectToUser() { }
    }
}

fun userProtocol(): GuessingGameCallbacksUser {
    return object : GuessingGameCallbacksUser {
        var currMin: Long = Long.MIN_VALUE
        var currMax: Long = Long.MAX_VALUE
        var guess: Long = Long.MIN_VALUE

        override fun onSendGuessToMagician(): Long {
            guess = guess(currMin, currMax)
            return guess
        }

        override fun onReceiveMaxFromMagician(v: Long) {
            currMax = v
        }
        override fun onReceiveMinFromMagician(v: Long) {
            currMin = v
        }

        override fun onReceiveDummyHigherFromMagician() { currMin = guess + 1 }
        override fun onReceiveDummyLowerFromMagician() { currMax = guess - 1 }
        override fun onReceiveDummyCorrectFromMagician() { }
    }
}
