package app.impl

import channelsKey
import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import socketsKey
import twoBuyerIterations
import twobuyer.ClientA
import twobuyer.ClientB
import twobuyer.Seller
import twobuyer.fluent.*
import java.util.*

val date = Date()

fun twoBuyer(backend: String) {
    when (backend) {
        channelsKey -> {
            twoBuyerChannels()
        }
        socketsKey -> {
            twoBuyerSockets()
        }
        else -> throw RuntimeException()
    }
}

fun twoBuyerChannels() {
    val chanA_Seller = SKChannel()
    val chanB_Seller = SKChannel()
    val chanA_B = SKChannel()

    runBlocking {
        launch {
            // Seller
            SKMPEndpoint().use { e ->
                e.connect(ClientA, chanA_Seller)
                e.connect(ClientB, chanB_Seller)
                twoBuyerSeller(e)
            }
        }
        launch {
            // Client A
            SKMPEndpoint().use { e ->
                e.connect(Seller, chanA_Seller)
                e.connect(ClientB, chanA_B)
                twoBuyerClientA(e)
            }
        }
        launch {
            // Client B
            SKMPEndpoint().use { e ->
                e.connect(Seller, chanB_Seller)
                e.connect(ClientA, chanA_B)
                twoBuyerClientB(e)
            }
        }
    }
}

fun twoBuyerSockets() {
    val sellerPortChan = Channel<Int>()
    val bPortChan = Channel<Int>()

    runBlocking {
        launch {
            // Seller
            SKMPEndpoint().use { e ->
                val s = SKMPEndpoint.bind()
                sellerPortChan.send(s.port) // for A
                sellerPortChan.send(s.port) // for B
                e.accept(ClientA, s)
                e.accept(ClientB, s)

                twoBuyerSeller(e)
            }
        }
        launch {
            // Client A
            SKMPEndpoint().use { e ->
                e.request(Seller, "localhost", sellerPortChan.receive())
                e.request(ClientB, "localhost", bPortChan.receive())

                twoBuyerClientA(e)
            }
        }
        launch {
            // Client B
            SKMPEndpoint().use { e ->
                val s = SKMPEndpoint.bind()
                bPortChan.send(s.port)
                e.request(Seller, "localhost", sellerPortChan.receive())
                e.accept(ClientA, s)

                twoBuyerClientB(e)
            }
        }
    }
}

suspend fun twoBuyerClientA(e: SKMPEndpoint) {
    var b1: TwoBuyerClientA1Interface = TwoBuyerClientA1(e)
    var price = 0
    repeat(twoBuyerIterations) {
        b1 = b1.sendIdToSeller("")
            .receivePriceFromSeller { price = it }
            .sendaShareToClientB(price / 2)
            .branch()
            .let {
                when (it) {
                    is TwoBuyerClientA6_DateInterface -> it.receiveDateFromClientB { }
                    is TwoBuyerClientA6_RejectInterface -> it.receiveRejectFromClientB()
                }
            }
    }
    b1.sendQuitToSeller()
}

suspend fun twoBuyerClientB(e: SKMPEndpoint) {
    var b1: TwoBuyerClientB1Branch? = TwoBuyerClientB1(e).branch()

    while (b1 != null) {
        lateinit var receivedDate: Date

        b1 = when (b1) {
            is TwoBuyerClientB1_PriceInterface -> b1.receivePriceFromSeller { }
                .receiveaShareFromClientA { }
                .sendAddressToSeller("an address")
                .receiveDateFromSeller { receivedDate = it }
                .sendDateToClientA(receivedDate)
                .branch()
            is TwoBuyerClientB1_QuitInterface -> {
                b1.receiveQuitFromSeller()
                null
            }
        }
    }
}

suspend fun twoBuyerSeller(e: SKMPEndpoint) {
    var b1: TwoBuyerSeller1Branch? = TwoBuyerSeller1(e).branch()

    while (b1 != null) {
        b1 = when (b1) {
            is TwoBuyerSeller1_IdInterface -> b1.receiveIdFromClientA { }
                .sendPriceToClientA(10)
                .sendPriceToClientB(10)
                .branch()
                .let { b6 ->
                    when (b6) {
                        is TwoBuyerSeller6_AddressInterface -> b6.receiveAddressFromClientB { }
                            .sendDateToClientB(date)
                            .branch()
                        is TwoBuyerSeller6_RejectInterface -> b6.receiveRejectFromClientB().branch()
                    }
                }
            is TwoBuyerSeller1_QuitInterface -> {
                b1.receiveQuitFromClientA().sendQuitToClientB()
                null
            }
        }
    }
}