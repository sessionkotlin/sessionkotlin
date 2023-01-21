package dsl.examples

import com.github.sessionkotlin.lib.dsl.SKRole
import com.github.sessionkotlin.lib.dsl.globalProtocolInternal
import org.junit.jupiter.api.Test

class GuessTheNumber {

    @Test
    fun main() {
        val guesser = SKRole("Guesser")
        val middleman = SKRole("Middleman")
        val chooser = SKRole("Chooser")

        globalProtocolInternal {
            send<Int>(chooser, middleman, "num", "num > 0 && num < 100")
            val t = mu()
            send<Int>(guesser, middleman, "guess",)
            choice(middleman) {
                branch {
                    send<Unit>(middleman, guesser, "lower", "guess > num")
                    goto(t)
                }
                branch {
                    send<Unit>(middleman, guesser, "higher", "guess < num")
                    goto(t)
                }
                branch {
                    send<Unit>(middleman, guesser, "correct", "guess == num")
                }
            }
        }
    }
}
