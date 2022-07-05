package com.github.d_costa.sessionkotlin.api

import com.github.d_costa.sessionkotlin.api.exception.SKLinearException
import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.message.SKDummyMessage
import com.github.d_costa.sessionkotlin.backend.message.SKMessage

/**
 * Linear endpoint. Throws [SKLinearException] when [SKLinearEndpoint.use] is called more than once.
 */
public open class SKLinearEndpoint {
    private var used = false
    internal fun use() {
        if (used)
            throw SKLinearException()
        else
            used = true
    }
}

/**
 * Abstract linear endpoint that corresponds to an output.
 */
public abstract class SKOutputEndpoint(private val e: SKMPEndpoint) : SKInputEndpoint(e) {

    /**
     * Sends a message with payload of type [T] to the target [role].
     *
     */
    protected suspend fun <T : Any> send(role: SKGenRole, payload: T, label: String) {
        use()
        if (payload is Unit) {
            e.send(role, SKDummyMessage(label))
        } else {
            e.send(role, SKMessage(label, payload))
        }
    }
}

/**
 * Abstract linear endpoint that corresponds to an input.
 */
public abstract class SKInputEndpoint(private val e: SKMPEndpoint) : SKLinearEndpoint() {

    protected open suspend fun receive(role: SKGenRole): SKMessage {
        use()
        return e.receive(role)
    }

    /**
     * Receive a message with payload of type [T] from [role]
     * and assign its payload to [buf]'s value.
     */
    @Suppress("unchecked_cast")
    protected suspend fun <T : Any> receive(role: SKGenRole, buf: SKBuffer<T>) {
        val msg = receive(role)
        buf.value = msg.payload as T
    }
}

public abstract class SKCaseEndpoint(e: SKMPEndpoint, private val msg: SKMessage) : SKInputEndpoint(e) {
    protected override suspend fun receive(role: SKGenRole): SKMessage {
        use()
        return msg
    }
}
