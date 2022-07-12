package impl

import org.scribble.runtime.net.BinaryChannelEndpoint
import org.scribble.runtime.net.ScribServerSocket
import org.scribble.runtime.net.SocketChannelEndpoint
import org.scribble.runtime.session.SessionEndpoint
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.channels.ServerSocketChannel

/**
 * Pre: [socketChannel] open() and bind()
 */
class CustomScribbleServerSocket(private val socketChannel: ServerSocketChannel): ScribServerSocket(socketChannel.socket().localPort) {

    override fun close() {
        try {
            socketChannel.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun accept(se: SessionEndpoint<*, *>?): BinaryChannelEndpoint {
        return SocketChannelEndpoint(se, socketChannel.accept())
    }

}

fun newCustomServerSocket(): ServerSocketChannel =
    ServerSocketChannel
        .open()
        .bind(InetSocketAddress(0))
