package com.github.d_costa.sessionkotlin.backend.message

import mu.KotlinLogging
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
 */
internal class ObjectFormatter : SKMessageFormatter {
    private val intSize = 4
    private val logger = KotlinLogging.logger {}

    override fun toBytes(msg: SKMessage): ByteArray {
        val bytes = serialize(msg)
        val b = ByteBuffer.allocate(intSize + bytes.size)
        b.putInt(bytes.size)
        b.put(bytes)
        return b.array()
    }

    override fun fromBytes(b: ByteBuffer): Optional<SKMessage> {
        println(b.remaining())
        if (b.remaining() <= intSize) {
            logger.error { "Invalid msg size: ${b.remaining()}" }
            return Optional.empty()
        }

        // Get without changing the position
        val size = ByteBuffer.wrap(Arrays.copyOf(b.array(), intSize)).int

        if (b.remaining() >= intSize + size) {
            b.position(b.position() + intSize) // skip size
            val msgBytes = ByteArray(size)
            b.get(msgBytes) // get message
            b.compact()
            b.flip()
            return Optional.of(deserialize(msgBytes))
        }
        logger.error { "Msg size $size expected, got ${b.remaining()}" }
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
