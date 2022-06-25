package com.github.d_costa.sessionkotlin.backend.message

/**
 * The message formatter.
 *
 * An implementation of this class is necessary to send messages in sockets.
 */
public interface SKMessageFormatter {
    public fun toBytes(msg: SKMessage): ByteArray
    public fun fromBytes(b: ByteArray): SKMessage
}
