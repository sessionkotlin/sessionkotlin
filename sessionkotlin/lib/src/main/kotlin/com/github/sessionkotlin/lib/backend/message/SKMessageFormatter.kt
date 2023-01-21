package com.github.sessionkotlin.lib.backend.message

import java.nio.ByteBuffer
import java.util.*

/**
 * The message formatter.
 **
 */
public interface SKMessageFormatter {
    public fun toBytes(msg: SKMessage): ByteArray

    /**
     * If the provided bytes are not enough to form a valid message,
     * [Optional.empty()] should be thrown.
     */
    public fun fromBytes(b: ByteBuffer): Optional<SKMessage>
}
