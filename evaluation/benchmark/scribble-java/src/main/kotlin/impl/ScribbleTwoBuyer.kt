package impl

import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.scribble.runtime.message.ObjectStreamFormatter
import org.scribble.runtime.net.SocketChannelEndpoint
import org.scribble.runtime.net.SocketChannelServer
import org.scribble.runtime.session.MPSTEndpoint
import org.scribble.runtime.util.Buf
import twoBuyerIterations
import twobuyer.TwoBuyer.TwoBuyer.TwoBuyer
import twobuyer.TwoBuyer.TwoBuyer.ops.*
import twobuyer.TwoBuyer.TwoBuyer.roles.A
import twobuyer.TwoBuyer.TwoBuyer.roles.B
import twobuyer.TwoBuyer.TwoBuyer.roles.Seller
import twobuyer.TwoBuyer.TwoBuyer.statechans.A.TwoBuyer_A_1
import twobuyer.TwoBuyer.TwoBuyer.statechans.A.ioifaces.Branch_A_B_date_Date__B_reject.*
import twobuyer.TwoBuyer.TwoBuyer.statechans.B.TwoBuyer_B_1
import twobuyer.TwoBuyer.TwoBuyer.statechans.B.ioifaces.Branch_B_Seller_Quit__Seller_price_int.*
import twobuyer.TwoBuyer.TwoBuyer.statechans.Seller.TwoBuyer_Seller_1
import twobuyer.TwoBuyer.TwoBuyer.statechans.Seller.ioifaces.Branch_Seller_A_Quit__A_id_String.*
import twobuyer.TwoBuyer.TwoBuyer.statechans.Seller.ioifaces.Branch_Seller_B_address_String__B_reject.*
import java.util.*
import kotlin.concurrent.thread

val currDate = Date()


fun twoBuyer() {
    val session = TwoBuyer()
    runBlocking(Dispatchers.IO) {

        launch {
            // Client A
            MPSTEndpoint(session, A.A, ObjectStreamFormatter()).use { e ->
                e.request(Seller.Seller, ::SocketChannelEndpoint, "localhost", 9998)
                println("A requested seller")

                e.request(B.B, ::SocketChannelEndpoint, "localhost", 9997)
                println("A requested b")
                clientAProtocol(e)
            }
        }
        launch  {
            // Client B
            MPSTEndpoint(session, B.B, ObjectStreamFormatter()).use { e ->


                e.accept(SocketChannelServer(9997), A.A)
                println("B accepted A")

                e.request(Seller.Seller, ::SocketChannelEndpoint, "localhost", 9999)
                println("B requested seller")

                clientBProtocol(e)
            }
        }
        launch {
            // Seller
            MPSTEndpoint(session, Seller.Seller, ObjectStreamFormatter()).use { e ->
                e.accept(SocketChannelServer(9998), A.A)
                println("seller accepted a")


                e.accept(SocketChannelServer(9999), B.B)
                println("seller accepted b")

                sellerProtocol(e)
            }
        }
    }
}

fun sellerProtocol(e: MPSTEndpoint<TwoBuyer, Seller>) {
    var cases = TwoBuyer_Seller_1(e)
        .branch(A.A)

    while (cases != null) {
        cases = when (cases.op) {
            Branch_Seller_A_Quit__A_id_String_Enum.id -> cases
                .receive(A.A, id.id, Buf())
                .send(A.A, price.price, 0)
                .send(B.B, price.price, 0)
                .branch(B.B)
                .let {
                    when (it.op) {
                        Branch_Seller_B_address_String__B_reject_Enum.address -> it
                            .receive(address.address)
                            .send(B.B, date.date, currDate)
                            .branch(A.A)
                        Branch_Seller_B_address_String__B_reject_Enum.reject -> it
                            .receive(reject.reject)
                            .branch(A.A)
                        null -> throw RuntimeException()
                    }
                }
            Branch_Seller_A_Quit__A_id_String_Enum.Quit -> {
                cases.receive(A.A, Quit.Quit)
                null
            }
            null -> throw RuntimeException()
        }
    }

}

fun clientAProtocol(e: MPSTEndpoint<TwoBuyer, A>) {
    val priceBuf = Buf<Int>()

    var b = TwoBuyer_A_1(e)

    repeat(twoBuyerIterations) {
        println()
        TwoBuyer_A_1(e)
        b = b.send(Seller.Seller, id.id, "")
            .receive(Seller.Seller, price.price, priceBuf)
            .send(B.B, aShare.aShare, priceBuf.`val` / 2)
            .branch(B.B)
            .let {
                when (it.op) {
                    Branch_A_B_date_Date__B_reject_Enum.date -> it.receive(date.date)
                    Branch_A_B_date_Date__B_reject_Enum.reject -> it.receive(date.date, Buf())
                    null -> throw RuntimeException()
                }
            }
    }
    b.send(Seller.Seller, Quit.Quit)


}

fun clientBProtocol(e: MPSTEndpoint<TwoBuyer, B>) {
    val dateBuf = Buf<Date>()
    var b: TwoBuyer_B_1? = TwoBuyer_B_1(e)

    while (b != null) {
        b = b.branch(Seller.Seller)
            .let {
                when (it.op) {
                    Branch_B_Seller_Quit__Seller_price_int_Enum.price -> it
                        .receive(price.price)
                        .receive(A.A, aShare.aShare, Buf())
                        .send(Seller.Seller, address.address, "")
                        .receive(Seller.Seller, date.date, dateBuf)
                        .send(A.A, date.date, dateBuf.`val`)
                    Branch_B_Seller_Quit__Seller_price_int_Enum.Quit -> {
                        it.receive(Quit.Quit)
                        null
                    }
                    null -> throw RuntimeException()
                }
            }
    }
}

