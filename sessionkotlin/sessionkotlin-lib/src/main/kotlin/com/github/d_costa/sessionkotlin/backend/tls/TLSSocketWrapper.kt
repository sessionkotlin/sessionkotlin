package com.github.d_costa.sessionkotlin.backend.tls

import com.github.d_costa.sessionkotlin.backend.endpoint.ConnectionEnd
import com.github.d_costa.sessionkotlin.backend.endpoint.SocketWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import java.nio.ByteBuffer
import javax.net.ssl.KeyManager
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLEngine
import javax.net.ssl.SSLEngineResult.HandshakeStatus
import javax.net.ssl.SSLEngineResult.Status
import javax.net.ssl.TrustManager

/**
 * Secure a socket with TLS.
 *
 * @param connectionEnd Whether to behave as a client or a server
 * @param keyManagers List of keyManagers. May be null in which case the installed security providers will be searched
 * for the highest priority implementation of the appropriate factory.
 * @param trustManagers List of trust managers. May be null in which case the installed security providers will be searched
 * for the highest priority implementation of the appropriate factory.
 * @param debug Whether to log debug information
 *
 */
public class TLSSocketWrapper(
    private val connectionEnd: ConnectionEnd,
    keyManagers: Collection<KeyManager>? = null,
    trustManagers: Collection<TrustManager>? = null,
    private val debug: Boolean = false,
) : SocketWrapper() {

    private val sslEngine: SSLEngine

//    /**
//     * Encoded outbound data
//     */
//    private var netData: ByteBuffer
//
    /**
     * Decoded inbound data
     */
    private var peerAppData: ByteBuffer

    /**
     * Encoded inbound data
     */
//    private var peerNetData: ByteBuffer

    private val emptyByteBuffer = ByteBuffer.allocate(0)
    private val logger = KotlinLogging.logger(this::class.simpleName!!)

    init {
        // https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#SSLEngine

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(keyManagers?.toTypedArray(), trustManagers?.toTypedArray(), null)
        sslEngine = sslContext.createSSLEngine()

        val session = sslEngine.session
        peerAppData = ByteBuffer.allocate(session.applicationBufferSize)
    }

    override suspend fun wrapBytes(buffer: ByteBuffer): ByteBuffer {
        val netData = ByteBuffer.allocate(sslEngine.session.packetBufferSize)

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
        val peerAppData = ByteBuffer.allocate(sslEngine.session.applicationBufferSize)

        while (wrappedBytes.hasRemaining()) {
            val result = withContext(Dispatchers.IO) {
                sslEngine.unwrap(wrappedBytes, peerAppData)
            }

            when (result.status) {
                Status.OK -> {}
                Status.CLOSED -> {
                    logger.info { "$connectionEnd: Peer closed the connection" }
                }
                else -> {
                    TODO()
                }
            }
        }

        peerAppData.flip()
        return peerAppData
    }

    override suspend fun readBytes(destBuffer: ByteBuffer) {
        // check for any leftover data
        // peerAppData is in write mode, so:
        peerAppData.flip()

        if (peerAppData.hasRemaining()) {
            destBuffer.put(peerAppData)
            peerAppData.compact()
        } else {
            // back to write mode
            peerAppData.compact()
            super.readBytes(destBuffer)
        }
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
        val netData = ByteBuffer.allocate(sslEngine.session.packetBufferSize)
        val peerNetData = ByteBuffer.allocate(sslEngine.session.packetBufferSize)

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
                            Status.OK -> {
                            }
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

        // Check for leftover data
        peerNetData.flip()

        if (peerNetData.hasRemaining()) {
            peerAppData.put(unwrapBytes(peerNetData))
        }

        peerNetData.compact()

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
        val netData = ByteBuffer.allocate(sslEngine.session.packetBufferSize)

        while (!sslEngine.isOutboundDone) {
            withContext(Dispatchers.IO) {
                // Get close message
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
