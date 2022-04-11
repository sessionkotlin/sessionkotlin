package org.david.sessionkotlin_lib.api

import io.ktor.util.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.nio.ByteBuffer
import java.util.*

internal interface SKMessageFormatter {
    fun toBytes(msg: SKMessage): ByteArray
    fun fromBytes(bb: ByteArray): SKMessage
}

internal class ObjectFormatter: SKMessageFormatter {

    override fun toBytes(msg: SKMessage): ByteArray {
        return serialize(msg)
    }

    override fun fromBytes(bb: ByteArray): SKMessage {
        return deserialize(bb)
    }

    private fun serialize(obj: Any): ByteArray {
        ByteArrayOutputStream().use { bos ->
            ObjectOutputStream(bos).use {
                it.writeObject(obj)
                return bos.toByteArray()
            }
        }
    }

    private fun deserialize(b: ByteArray): SKMessage {
        ByteArrayInputStream(b).use { bis ->
            ObjectInputStream(bis).use {
                return it.readObject() as SKMessage
            }
        }
    }
}

