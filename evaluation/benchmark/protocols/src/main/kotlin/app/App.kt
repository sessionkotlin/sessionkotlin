package app

import com.github.d_costa.sessionkotlin.dsl.SKRole
import com.github.d_costa.sessionkotlin.dsl.globalProtocol

fun main() {
    val guesser = SKRole("User")
    val magician = SKRole("Magician")
    globalProtocol("Guessing Game", callbacks = true) {
        send<Long>(magician, guesser, "min")
        send<Long>(magician, guesser, "max")

        val t = mu()
        send<Long>(guesser, magician, "guess")
        choice(magician) {
            branch("Higher") {
                send<Unit>(magician, guesser, "dummyHigher")
                goto(t)
            }
            branch("Lower") {
                send<Unit>(magician, guesser, "dummyLower")
                goto(t)
            }
            branch("Correct") {
                send<Unit>(magician, guesser, "dummyCorrect")
            }
        }
    }
    val client = SKRole("Client")
    val server = SKRole("Server")
    globalProtocol("Adder") {
        choice(client) {
            branch("Quit") {
                send<Unit>(client, server)
            }
            branch("Add") {
                send<Int>(client, server)
                send<Int>(client, server)
                send<Int>(server, client)
            }
        }
    }
}

