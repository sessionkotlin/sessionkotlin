package org.david.sessionkotlin_lib.backend

internal interface SKMessageFormatter {
    fun toBytes(msg: SKMessage): ByteArray
    fun fromBytes(b: ByteArray): SKMessage
}
