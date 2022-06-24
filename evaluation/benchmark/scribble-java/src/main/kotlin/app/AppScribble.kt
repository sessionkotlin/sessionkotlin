package app

import guessing_game.GuessingGame.GuessingGame.GuessingGame
import guessing_game.GuessingGame.GuessingGame.ops.*
import guessing_game.GuessingGame.GuessingGame.roles.Magician
import guessing_game.GuessingGame.GuessingGame.roles.User
import guessing_game.GuessingGame.GuessingGame.statechans.Magician.GuessingGame_Magician_1
import guessing_game.GuessingGame.GuessingGame.statechans.User.GuessingGame_User_1
import guessing_game.GuessingGame.GuessingGame.statechans.User.ioifaces.Branch_User_Magician_Correct__Magician_Higher__Magician_Lower.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import lib.guess
import lib.randomLong
import maxValue
import minValue
import org.scribble.runtime.message.ObjectStreamFormatter
import org.scribble.runtime.net.SocketChannelEndpoint
import org.scribble.runtime.net.SocketChannelServer
import org.scribble.runtime.session.MPSTEndpoint
import org.scribble.runtime.util.Buf
import kotlin.coroutines.CoroutineContext
import kotlin.system.exitProcess

const val port = 8888

fun main(args: Array<String>) {
    val usage = "Usage: ./gradlew scribble-java"
    if (args.size != 0) {
        println(usage)
        exitProcess(1)
    }
    val secretNumber = randomLong()
    scribble(secretNumber)
}

fun scribble(secretNumber: Long) {
    val session = GuessingGame()

    runBlocking {
        launch(Dispatchers.IO) {
            MPSTEndpoint(session, Magician.Magician, ObjectStreamFormatter()).use { e ->
                e.accept(SocketChannelServer(port), User.User)
                magicianProtocol(e, secretNumber)
            }
        }
        launch {
            MPSTEndpoint(session, User.User, ObjectStreamFormatter()).use { e ->
                e.request(Magician.Magician, ::SocketChannelEndpoint, "localhost", port)
                userProtocol(e)
            }
        }
    }

}

fun magicianProtocol(e: MPSTEndpoint<GuessingGame, Magician>, secretNumber: Long) {
    val buf = Buf<Long>()
    GuessingGame_Magician_1(e)
        .send(User.User, Min.Min, minValue)
        .send(User.User, Max.Max, maxValue)
        .let { initialState3 ->
            var state3 = initialState3
            do {
                var found = false
                val state4 = state3.receive(User.User, Guess.Guess, buf)

                if (buf.`val` < secretNumber) {
                    state3 = state4.send(User.User, Higher.Higher)
                } else if (buf.`val` > secretNumber) {
                    state3 = state4.send(User.User, Lower.Lower)
                } else {
                    state4.send(User.User, Correct.Correct)
                    found = true
                }
            } while (!found)
        }
}

fun userProtocol(e: MPSTEndpoint<GuessingGame, User>) {
    val buf = Buf<Long>()
    var currMin: Long
    var currMax: Long

    GuessingGame_User_1(e)
        .receive(Magician.Magician, Min.Min, buf).also { currMin = buf.`val` }
        .receive(Magician.Magician, Max.Max, buf).also { currMax = buf.`val` }
        .let { initialState3 ->
            var state3 = initialState3
            do {
                var found = false
                val guess = guess(currMin, currMax)

                val state4 = state3.send(Magician.Magician, Guess.Guess, guess)

                val cases = state4.branch(Magician.Magician)
                when(cases.op) {
                    Branch_User_Magician_Correct__Magician_Higher__Magician_Lower_Enum.Higher -> {
                        state3 = cases.receive(Higher.Higher)
                        currMin = guess + 1
                    }
                    Branch_User_Magician_Correct__Magician_Higher__Magician_Lower_Enum.Lower -> {
                        state3 = cases.receive(Lower.Lower)
                        currMax = guess - 1
                    }
                    Branch_User_Magician_Correct__Magician_Higher__Magician_Lower_Enum.Correct -> {
                        cases.receive(Correct.Correct)
                        found = true
                    }
                    else -> throw Exception("Enum argument can be null in Java, so we need an else branch to be exhaustive")
                }
            } while (!found)
        }
}
