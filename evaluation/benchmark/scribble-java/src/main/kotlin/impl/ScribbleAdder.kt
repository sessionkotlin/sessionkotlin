package impl

import adder.Adder.Adder.Adder
import adder.Adder.Adder.ops.quit
import adder.Adder.Adder.ops.sum
import adder.Adder.Adder.ops.v1
import adder.Adder.Adder.ops.v2
import adder.Adder.Adder.roles.Client
import adder.Adder.Adder.roles.Server
import adder.Adder.Adder.statechans.Client.Adder_Client_1
import adder.Adder.Adder.statechans.Server.Adder_Server_1
import adder.Adder.Adder.statechans.Server.ioifaces.Branch_Server_Client_quit__Client_v1_int
import adderIterations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.scribble.runtime.message.ObjectStreamFormatter
import org.scribble.runtime.net.SocketChannelEndpoint
import org.scribble.runtime.net.SocketChannelServer
import org.scribble.runtime.session.MPSTEndpoint
import org.scribble.runtime.util.Buf

fun adder() {
    val portChan = Channel<Int>()

    val session = Adder()
    runBlocking {
        launch(Dispatchers.IO) {
            MPSTEndpoint(session, Server.Server, ObjectStreamFormatter()).use { e ->
                val port = 9999
                portChan.send(9999)
                e.accept(SocketChannelServer(port), Client.Client)

                serverProtocol(e)
            }
        }
        launch {
            MPSTEndpoint(session, Client.Client, ObjectStreamFormatter()).use { e ->
                e.request(Server.Server, ::SocketChannelEndpoint, "localhost", portChan.receive())
                clientProtocol(e)
            }
        }
    }
}

fun serverProtocol(e: MPSTEndpoint<Adder, Server>) {
    var cases = Adder_Server_1(e)
        .branch(Client.Client)

    val buf1 = Buf<Int>()
    val buf2 = Buf<Int>()

    while (cases != null) {
        cases = when (cases.op) {
            Branch_Server_Client_quit__Client_v1_int.Branch_Server_Client_quit__Client_v1_int_Enum.quit -> {
                cases.receive(quit.quit)
                null
            }
            Branch_Server_Client_quit__Client_v1_int.Branch_Server_Client_quit__Client_v1_int_Enum.v1 ->
                cases.receive(v1.v1, buf1)
                    .receive(Client.Client, v2.v2, buf2)
                    .send(Client.Client, sum.sum, buf1.`val` + buf2.`val`)
                    .branch(Client.Client)
            null -> throw RuntimeException()
        }
    }
}

fun clientProtocol(e: MPSTEndpoint<Adder, Client>) {
    var index = 0

    var b =  Adder_Client_1(e)

    repeat(adderIterations) {
        b = b
            .send(Server.Server, v1.v1, index)
            .send(Server.Server, v2.v2, index++)
            .receive(Server.Server, sum.sum, Buf())
    }
    b.send(Server.Server, quit.quit)
}
