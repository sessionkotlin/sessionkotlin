package com.github.d_costa.sessionkotlin.backend.message

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.ByteBuffer
import java.util.*

/**
 * Default implementation of the message formatter.
 *
 * Internally uses [ObjectOutputStream] and [ObjectInputStream].
 *
 * Sends the message size before the message.
 */
internal class ObjectFormatter : SKMessageFormatter {

    override fun toBytes(msg: SKMessage): ByteArray {
        val bytes = serialize(msg)
        val b = ByteBuffer.allocate(Int.SIZE_BYTES + bytes.size)
        b.putInt(bytes.size)
        b.put(bytes)
        return b.array()
    }

    override fun fromBytes(b: ByteBuffer): Optional<SKMessage> {
        if (b.remaining() <= Int.SIZE_BYTES) {
            // Nothing to read
            return Optional.empty()
        }

        // Get the integer without modifying the position
        val size = ByteBuffer.wrap(Arrays.copyOfRange(b.array(), b.position(), b.position() + Int.SIZE_BYTES)).int

        if (b.remaining() >= Int.SIZE_BYTES + size) {
            b.position(b.position() + Int.SIZE_BYTES) // skip size
            val msgBytes = ByteArray(size)
            b.get(msgBytes) // get message
            return Optional.of(deserialize(msgBytes))
        }
        return Optional.empty()
    }

    private fun deserialize(b: ByteArray): SKMessage {
        ByteArrayInputStream(b).use { bis ->
            ObjectInputStream(bis).use {
                return it.readObject() as SKMessage
            }
        }
    }

    private fun serialize(msg: SKMessage): ByteArray {
        ByteArrayOutputStream().use { bos ->
            ObjectOutputStream(bos).use {
                it.writeObject(msg)
                return bos.toByteArray()
            }
        }
    }
}
