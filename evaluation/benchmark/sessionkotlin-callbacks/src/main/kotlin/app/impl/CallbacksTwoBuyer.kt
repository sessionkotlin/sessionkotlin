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
import twobuyer.callbacks.*
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
            TwoBuyerSellerCallbacksEndpoint(twoBuyerSellerCallbacks()).use { e ->
                e.connect(ClientA, chanA_Seller)
                e.connect(ClientB, chanB_Seller)

                e.start()
            }
        }
        launch {
            // Client A
            TwoBuyerClientACallbacksEndpoint(twoBuyerClientACallbacks()).use { e ->
                e.connect(ClientB, chanA_B)
                e.connect(Seller, chanA_Seller)

                e.start()
            }
        }
        launch {
            // Client B
            TwoBuyerClientBCallbacksEndpoint(twoBuyerClientBCallbacks()).use { e ->
                e.connect(ClientA, chanA_B)
                e.connect(Seller, chanB_Seller)

                e.start()
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
            TwoBuyerSellerCallbacksEndpoint(twoBuyerSellerCallbacks()).use { e ->
                val s = SKMPEndpoint.bind()
                sellerPortChan.send(s.port) // for A
                sellerPortChan.send(s.port) // for B
                e.accept(ClientA, s)
                e.accept(ClientB, s)

                e.start()
            }
        }
        launch {
            // Client A
            TwoBuyerClientACallbacksEndpoint(twoBuyerClientACallbacks()).use { e ->
                e.request(Seller, "localhost", sellerPortChan.receive())
                e.request(ClientB, "localhost", bPortChan.receive())

                e.start()
            }
        }
        launch {
            // Client B
            TwoBuyerClientBCallbacksEndpoint(twoBuyerClientBCallbacks()).use { e ->
                val s = SKMPEndpoint.bind()
                bPortChan.send(s.port)
                e.request(Seller, "localhost", sellerPortChan.receive())
                e.accept(ClientA, s)

                e.start()
            }
        }
    }
}

fun twoBuyerClientACallbacks(): TwoBuyerClientACallbacks {
    var index = 0
    var price = 0
    return object : TwoBuyerClientACallbacks {
        override fun onChoice1(): Choice1 = if (index++ < twoBuyerIterations) Choice1.Choice1_Id else Choice1.Choice1_Quit
        override fun sendIdToSeller(): String = ""
        override fun sendQuitToSeller() { }
        override fun receivePriceFromSeller(v: Int) {
            price = v
        }
        override fun sendaShareToClientB() = price / 2
        override fun receiveDateFromClientB(v: Date) { }
        override fun receiveRejectFromClientB(v: Unit) { }
    }
}

fun twoBuyerClientBCallbacks(): TwoBuyerClientBCallbacks {
    lateinit var receivedDate: Date

    return object: TwoBuyerClientBCallbacks {
        override fun receivePriceFromSeller(v: Int) { }
        override fun receiveQuitFromSeller(v: Unit) { }
        override fun receiveaShareFromClientA(v: Int) { }
        override fun onChoice5() = Choice5.Choice5_Address
        override fun sendAddressToSeller() = ""
        override fun sendRejectToSeller() { }
        override fun receiveDateFromSeller(v: Date) {
            receivedDate = v
        }
        override fun sendDateToClientA() = receivedDate
        override fun sendRejectToClientA() { }
    }
}

fun twoBuyerSellerCallbacks(): TwoBuyerSellerCallbacks {
    return object: TwoBuyerSellerCallbacks {
        override fun receiveIdFromClientA(v: String) { }
        override fun receiveQuitFromClientA(v: Unit) { }
        override fun sendPriceToClientA(): Int = 0
        override fun sendPriceToClientB(): Int = 0
        override fun receiveAddressFromClientB(v: String) { }
        override fun receiveRejectFromClientB(v: Unit) { }
        override fun sendDateToClientB() = date
        override fun sendQuitToClientB() { }
    }
}