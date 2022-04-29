package org.david.sessionkotlin.backend

/**
 * The message formatter.
 *
 * An implementation of this class is necessary to send messages in sockets.
 */
internal interface SKMessageFormatter {
    fun toBytes(msg: SKMessage): ByteArray
    fun fromBytes(b: ByteArray): SKMessage
}
