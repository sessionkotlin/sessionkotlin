package impl

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.util.network.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import twobuyer.callbacks.*
import java.util.*

fun twoBuyerManualChannels() {
    val sellerChan = Channel<Any>()
    val bChan = Channel<Any>()
    val aChan = Channel<Any>()

    runBlocking {
        launch {
            // Seller
            repeat(twoBuyerIterations) {
                sellerChan.receive() as String
                aChan.send(10)
                bChan.send(10)

                val res = sellerChan.receive()

                if (res !is String)
                    return@launch

                bChan.send(Date())
            }
        }
        launch {
            // Client A
            repeat(twoBuyerIterations) {
                sellerChan.send("myId")
                val price = aChan.receive() as Int
                bChan.send(price / 2)

                if (aChan.receive() !is Date) {
                    return@launch
                }
            }
        }
        launch {
            // Client B
            repeat(twoBuyerIterations) {
                bChan.receive() as Int
                bChan.receive() as Int
                sellerChan.send("an address")
                val d = bChan.receive() as Date
                aChan.send(d)
            }
        }
    }
}
fun twoBuyerManualSockets(sellerSocketA: ServerSocket, sellerSocketB: ServerSocket, bSocket: ServerSocket,
selector: SelectorManager) {
    runBlocking {
        launch {
            // Seller
            val toA = sellerSocketA.accept()
            val aOut = toA.openWriteChannel(autoFlush = true)
            val aIn = toA.openReadChannel()

            val toB = sellerSocketB.accept()
            val bOut = toB.openWriteChannel(autoFlush = true)
            val bIn = toB.openReadChannel()

            repeat(twoBuyerIterations) {
                val id = aIn.readUTF8Line()
                aOut.writeInt(10)
                bOut.writeInt(10)

                val address = bIn.readUTF8Line()
                bOut.writeStringUtf8(Date().toString() + "\n")
            }
        }
        launch {
            // Client A
            val toSeller = aSocket(selector).tcp().connect("localhost", sellerSocketA.localAddress.toJavaAddress().port)
            val sellerOut = toSeller.openWriteChannel(autoFlush = true)
            val sellerIn = toSeller.openReadChannel()

            val toB = aSocket(selector).tcp().connect("localhost", bSocket.localAddress.toJavaAddress().port)
            val bOut = toB.openWriteChannel(autoFlush = true)
            val bIn = toB.openReadChannel()

            repeat(twoBuyerIterations) {
                sellerOut.writeStringUtf8("myId\n")
                val price = sellerIn.readInt()
                bOut.writeInt(price / 2)
                val date = bIn.readUTF8Line()
            }
        }
        launch {
            // Client B
            val toSeller = aSocket(selector).tcp().connect("localhost", sellerSocketB.localAddress.toJavaAddress().port)
            val sellerOut = toSeller.openWriteChannel(autoFlush = true)
            val sellerIn = toSeller.openReadChannel()

            val toA = bSocket.accept()
            val aOut = toA.openWriteChannel(autoFlush = true)
            val aIn = toA.openReadChannel()

            repeat(twoBuyerIterations) {
                sellerIn.readInt()
                aIn.readInt()
                sellerOut.writeStringUtf8("an address\n")
                val date = sellerIn.readUTF8Line() ?: throw RuntimeException()
                aOut.writeStringUtf8(date + "\n")
            }
        }
    }
}
