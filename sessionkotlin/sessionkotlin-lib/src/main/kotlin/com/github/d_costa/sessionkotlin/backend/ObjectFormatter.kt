package com.github.d_costa.sessionkotlin.backend

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

/**
 * Default implementation of the message formatter.
 *
 * Internally uses [ObjectOutputStream] and [ObjectInputStream].
 */
internal class ObjectFormatter : SKMessageFormatter {

    override fun toBytes(msg: SKMessage): ByteArray {
        ByteArrayOutputStream().use { bos ->
            ObjectOutputStream(bos).use {
                it.writeObject(msg)
                return bos.toByteArray()
            }
        }
    }

    override fun fromBytes(b: ByteArray): SKMessage {
        ByteArrayInputStream(b).use { bis ->
            ObjectInputStream(bis).use {
                return it.readObject() as SKMessage
            }
        }
    }
}
