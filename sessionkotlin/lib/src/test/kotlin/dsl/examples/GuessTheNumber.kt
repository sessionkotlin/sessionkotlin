package dsl.examples

import org.david.sessionkotlin.dsl.SKRole
import org.david.sessionkotlin.dsl.globalProtocolInternal
import org.junit.jupiter.api.Test

class GuessTheNumber {

    @Test
    fun main() {
        val guesser = SKRole("Guesser")
        val middleman = SKRole("Middleman")
        val chooser = SKRole("Chooser")

        globalProtocolInternal {
            send<Int>(chooser, middleman, "num", "num > 0 && num < 100")
            val t = miu()
            send<Int>(guesser, middleman, "guess")
            choice(middleman) {
                branch("lower") {
                    send<Unit>(middleman, guesser, "guess > num")
                    goto(t)
                }
                branch("higher") {
                    send<Unit>(middleman, guesser, "guess < num")
                    goto(t)
                }
                branch("correct") {
                    send<Unit>(middleman, guesser, "guess == num")
                }
            }
        }
    }
}
