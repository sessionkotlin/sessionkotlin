package impl

import com.github.d_costa.sessionkotlin.backend.channel.SKChannel
import com.github.d_costa.sessionkotlin.backend.endpoint.SKServerSocket
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import twobuyer.ClientA
import twobuyer.ClientB
import twobuyer.Seller
import twobuyer.callbacks.*
import java.util.*

fun twoBuyerCallbacksChannels() {
    runBlocking {
        val chanA_Seller = SKChannel(ClientA, Seller)
        val chanB_Seller = SKChannel(ClientB, Seller)
        val chanA_B = SKChannel(ClientA, ClientB)

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

fun twoBuyerCallbacksSockets(serverSocket: SKServerSocket, clientBSocket: SKServerSocket) {
    runBlocking {
        launch {
            // Seller
            TwoBuyerSellerCallbacksEndpoint(twoBuyerSellerCallbacks()).use { e ->
                e.accept(ClientA, serverSocket)
                e.accept(ClientB, serverSocket)

                e.start()
            }
        }
        launch {
            // Client A
            TwoBuyerClientACallbacksEndpoint(twoBuyerClientACallbacks()).use { e ->
                e.request(Seller, "localhost", serverSocket.port)
                e.request(ClientB, "localhost", clientBSocket.port)

                e.start()
            }
        }
        launch {
            // Client B
            TwoBuyerClientBCallbacksEndpoint(twoBuyerClientBCallbacks()).use { e ->
                e.request(Seller, "localhost", serverSocket.port)
                e.accept(ClientA, clientBSocket)

                e.start()
            }
        }
    }
}

fun twoBuyerClientACallbacks(): TwoBuyerClientACallbacks {
    var index = 0
    var price = 0
    return object : TwoBuyerClientACallbacks {
        override fun onChoice1(): Choice1 =
            if (index++ < twoBuyerIterations) Choice1.Choice1_Id else Choice1.Choice1_Quit

        override fun sendIdToSeller(): String = ""
        override fun sendQuitToSeller() {}
        override fun receivePriceFromSeller(v: Int) {
            price = v
        }

        override fun sendaShareToClientB() = price / 2
        override fun receiveDateFromClientB(v: Date) {}
        override fun receiveRejectFromClientB(v: Unit) {}
    }
}

fun twoBuyerClientBCallbacks(): TwoBuyerClientBCallbacks {
    lateinit var receivedDate: Date

    return object : TwoBuyerClientBCallbacks {
        override fun receivePriceFromSeller(v: Int) {}
        override fun receiveQuitFromSeller(v: Unit) {}
        override fun receiveaShareFromClientA(v: Int) {}
        override fun onChoice3() = Choice3.Choice3_Address
        override fun sendAddressToSeller() = ""
        override fun sendRejectToSeller() {}
        override fun receiveDateFromSeller(v: Date) {
            receivedDate = v
        }

        override fun sendDateToClientA() = receivedDate
        override fun sendRejectToClientA() {}
    }
}

fun twoBuyerSellerCallbacks(): TwoBuyerSellerCallbacks {
    return object : TwoBuyerSellerCallbacks {
        override fun receiveIdFromClientA(v: String) {}
        override fun receiveQuitFromClientA(v: Unit) {}
        override fun sendPriceToClientA(): Int = 0
        override fun sendPriceToClientB(): Int = 0
        override fun receiveAddressFromClientB(v: String) {}
        override fun receiveRejectFromClientB(v: Unit) {}
        override fun sendDateToClientB() = Date()
        override fun sendQuitToClientB() {}
    }
}