package com.github.d_costa.sessionkotlin.backend.message

import java.nio.ByteBuffer
import java.util.*

/**
 * The message formatter.
 **
 */
public interface SKMessageFormatter {
    public companion object {
        public class SKInvalidMessage(msg: String) : RuntimeException(msg) {
            public constructor() : this("")
        }
    }
    public fun toBytes(msg: SKMessage): ByteArray

    /**
     * If the provided bytes are not enough to form a valid message,
     * [SKInvalidMessage] should be thrown.
     */
    public fun fromBytes(b: ByteBuffer): Optional<SKMessage>
}
