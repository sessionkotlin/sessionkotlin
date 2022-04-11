package demo

import Proto1_A_1
import Proto1_A_2_Case1
import Proto1_A_9_Case2
import Proto1_B_1
import Proto1_B_4_OK
import Proto1_B_6_Exit
import org.david.sessionkotlin_lib.api.SKBuffer


fun main() {
    val a1 = Proto1_A_1()
        .branch()
    when (a1) {
        is Proto1_A_2_Case1 -> {
            a1.receiveFromB(SKBuffer())
                .branchOK()
                .sendToB("abc")
                .branchOK()
                .sendToB("def")
                .branchExit()
                .sendToB()
        }
        is Proto1_A_9_Case2 ->
            a1.receiveFromB(SKBuffer())
    }

    val b1 = Proto1_B_1()
        .branchCase1()
        .sendToA(10)
        .branch()

    do {
        when (b1) {
            is Proto1_B_4_OK -> b1
                .receiveFromA(SKBuffer())
                .branch()

            is Proto1_B_6_Exit -> {
                b1.receiveFromA()
                break
            }
        }
    } while (true)

}
