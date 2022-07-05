package app.impl

import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.endpoint.SKServerSocket
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import impl.twoBuyerIterations
import twobuyer.ClientA
import twobuyer.ClientB
import twobuyer.Seller
import twobuyer.fluent.*
import java.util.*


fun twoBuyerFluentChannels() {
    runBlocking {
        val chanA_Seller = SKChannel(ClientA, Seller)
        val chanB_Seller = SKChannel(ClientB, Seller)
        val chanA_B = SKChannel(ClientA, ClientB)

        val j1 = launch {
            // Seller
            SKMPEndpoint().use { e ->
                e.connect(ClientA, chanA_Seller)
                e.connect(ClientB, chanB_Seller)
                twoBuyerSeller(e)
            }
        }
        val j2 = launch {
            // Client A
            SKMPEndpoint().use { e ->
                e.connect(Seller, chanA_Seller)
                e.connect(ClientB, chanA_B)
                twoBuyerClientA(e)
            }
        }
        val j3 =  launch {
            // Client B
            SKMPEndpoint().use { e ->
                e.connect(Seller, chanB_Seller)
                e.connect(ClientA, chanA_B)
                twoBuyerClientB(e)
            }
        }
        j1.join()
        j2.join()
        j3.join()
    }
}

fun twoBuyerFluentSockets(serverSocket: SKServerSocket, clientBSocket: SKServerSocket) {
    runBlocking {

        val j1 = launch {
            // Seller
            SKMPEndpoint().use { e ->
                e.accept(ClientA, serverSocket)
                e.accept(ClientB, serverSocket)

                twoBuyerSeller(e)
            }
        }
        val j2 = launch {
            // Client A
            SKMPEndpoint().use { e ->
                e.request(Seller, "localhost", serverSocket.port)
                e.request(ClientB, "localhost", clientBSocket.port)

                twoBuyerClientA(e)
            }
        }
        val j3 = launch {
            // Client B
            SKMPEndpoint().use { e ->
                e.request(Seller, "localhost", serverSocket.port)
                e.accept(ClientA, clientBSocket)

                twoBuyerClientB(e)
            }
        }
        j1.join()
        j2.join()
        j3.join()
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
                            .sendDateToClientB(Date())
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