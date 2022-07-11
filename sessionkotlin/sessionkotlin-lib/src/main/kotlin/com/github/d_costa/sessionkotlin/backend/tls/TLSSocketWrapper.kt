package com.github.d_costa.sessionkotlin.backend.tls

import com.github.d_costa.sessionkotlin.backend.endpoint.ConnectionEnd
import com.github.d_costa.sessionkotlin.backend.endpoint.SocketWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.nio.ByteBuffer
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult.HandshakeStatus
import javax.net.ssl.SSLEngineResult.Status

/**
 *  https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#SSLEngine
 *
 */
public class TLSSocketWrapper(
    private val connectionEnd: ConnectionEnd,
    private val debug: Boolean = false,
) : SocketWrapper() {

    private val sslEngine: SSLEngine

    /**
     * Encoded outbound data
     */
    private var netData: ByteBuffer

    /**
     * Decoded inbound data
     */
    private var peerAppData: ByteBuffer

    /**
     * Encoded inbound data
     */
    private var peerNetData: ByteBuffer

    private val emptyByteBuffer = ByteBuffer.allocate(0)
    private val logger = KotlinLogging.logger(this::class.simpleName!!)

    init {
        val sslContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, null, null)
        sslEngine = sslContext.createSSLEngine()

        val session = sslEngine.session
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

                        when (result.status) {
                            Status.OK -> {}
                            Status.BUFFER_UNDERFLOW -> {
                                // Need more source bytes
                                socketIO.readBytes(peerNetData)
                                done = false
                            }
                            else -> {
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
                        sslEngine.wrap(emptyByteBuffer, netData)
                    }
                    handshakeStatus = result.handshakeStatus

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
                            TODO()
                        }
                    }
                }

                HandshakeStatus.NEED_TASK -> {
                    // Handle blocking tasks
                    sslEngine.delegatedTask.run()
                    handshakeStatus = sslEngine.handshakeStatus
                }

                else -> {
                    TODO()
                }
            }
        }
        if (debug) {
            logger.info { "$connectionEnd: TLS Handshake complete" }
            logger.info { "$connectionEnd: Protocol: ${sslEngine.session.protocol}" }
            logger.info { "$connectionEnd: CipherSuite: ${sslEngine.session.cipherSuite}" }
        }
    }

    private suspend fun shutDown() {
        if (debug) {
            logger.info { "$connectionEnd: Closing connection..." }
        }

        sslEngine.closeOutbound()

        // Send handshake data
        netData.clear()

        while (!sslEngine.isOutboundDone) {
            withContext(Dispatchers.IO) {
                // flush any remaining handshake data
                sslEngine.wrap(emptyByteBuffer, netData)
            }

            netData.flip()

            while (netData.hasRemaining()) {
                socketIO.writeBytes(netData)
            }
            netData.compact()
        }

        if (debug) {
            logger.info { "$connectionEnd: Connection closed." }
        }
    }

    override fun close() {
        runBlocking { shutDown() }
        super.close()
    }
}
