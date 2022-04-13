package org.david.sessionkotlin_lib.backend

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

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
