package com.github.d_costa.sessionkotlin.backend.tls

import com.github.d_costa.sessionkotlin.backend.endpoint.ConnectionEnd
import com.github.d_costa.sessionkotlin.backend.endpoint.SocketWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult.HandshakeStatus
import javax.net.ssl.SSLEngineResult.Status

/**
 *  https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#SSLEngine
 *
 */
public class TLSSocketWrapper(private val connectionEnd: ConnectionEnd) : SocketWrapper() {

    private val sslEngine: SSLEngine

//    private var appData: ByteBuffer
    private var netData: ByteBuffer

    private var peerAppData: ByteBuffer
    private var peerNetData: ByteBuffer

    private val EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0)

    init {
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, null, null)
        sslEngine = sslContext.createSSLEngine()

        val session = sslEngine.session

//        appData = ByteBuffer.allocate(session.applicationBufferSize)
        netData = ByteBuffer.allocate(session.packetBufferSize)

        peerAppData = ByteBuffer.allocate(session.applicationBufferSize)
        peerNetData = ByteBuffer.allocate(session.packetBufferSize)
    }

    override suspend fun wrapBytes(buffer: ByteBuffer): ByteBuffer {
        val result = withContext(Dispatchers.IO) {
            sslEngine.wrap(buffer, netData)
        }

        when (result.status) {
            Status.OK -> {
            }
            else -> {
                println(result.status)
                TODO()
            }
        }

        netData.flip()
        return netData
    }

    override suspend fun unwrapBytes(wrappedBytes: ByteBuffer): ByteBuffer {
        while (wrappedBytes.hasRemaining()) {
            val result = withContext(Dispatchers.IO) {
                sslEngine.unwrap(wrappedBytes, peerAppData)
            }

            when (result.status) {
                Status.OK -> {
//                wrappedBytes.compact()
                }
                else -> {
                    println(result.status)
                    TODO()
                }
            }
        }

        peerAppData.flip()
        return peerAppData
    }

    override suspend fun handshake() {
        when (connectionEnd) {
            ConnectionEnd.Client -> sslEngine.useClientMode = true
            ConnectionEnd.Server -> {
                sslEngine.useClientMode = false
            }
        }
        doHandshake()
    }

    private suspend fun doHandshake() {

        withContext(Dispatchers.IO) {
            sslEngine.beginHandshake()
        }
        var handshakeStatus = sslEngine.handshakeStatus
        var counter = -1
        while (
            handshakeStatus != HandshakeStatus.FINISHED &&
            handshakeStatus != HandshakeStatus.NOT_HANDSHAKING
        ) {
            counter++
            when (handshakeStatus) {
                HandshakeStatus.NEED_UNWRAP -> {

                    do {
                        var done = true

                        // Receive handshaking data from peer
                        peerNetData.flip()
                        val result = withContext(Dispatchers.IO) {
                            sslEngine.unwrap(peerNetData, peerAppData)
                        }
                        peerNetData.compact()

                        handshakeStatus = result.handshakeStatus

//                        println("Need unwrap $counter, $result")

                        when (result.status) {
                            Status.OK -> {}
                            Status.BUFFER_UNDERFLOW -> {
                                socketIO.readBytes(peerNetData)
//                                println("underflow")
                                done = false
                            }
                            else -> {
                                println(result.status)
                                TODO()
                            }
                        }
                    } while (!done)
                }
                HandshakeStatus.NEED_WRAP -> {
                    // Empty the local network packet buffer.
                    netData.clear()

                    // Generate handshaking data
                    val result = withContext(Dispatchers.IO) {
                        sslEngine.wrap(EMPTY_BYTE_BUFFER, netData)
                    }
                    handshakeStatus = result.handshakeStatus

//                    println("Need wrap $counter, $result")

                    when (result.status) {
                        Status.OK -> {
                            netData.flip()

                            // Send the handshaking data to peer
                            while (netData.hasRemaining()) {
                                socketIO.writeBytes(netData)
                            }
                            netData.compact()
                        }
                        else -> {
                            println(result.status)
                            TODO()
                        }
                    }
                }

                HandshakeStatus.NEED_TASK -> {
                    // Handle blocking tasks
//                    println("Need task $counter")

                    sslEngine.delegatedTask.run()
                    handshakeStatus = sslEngine.handshakeStatus
//                    println("Need task $counter over")
                }

                else -> {
                    println(handshakeStatus)
                    TODO()
                }
            }
        }
        println(sslEngine.session)
    }
}
