package org.david.sessionkotlin.backend

internal interface SKMessageFormatter {
    fun toBytes(msg: SKMessage): ByteArray
    fun fromBytes(b: ByteArray): SKMessage
}
