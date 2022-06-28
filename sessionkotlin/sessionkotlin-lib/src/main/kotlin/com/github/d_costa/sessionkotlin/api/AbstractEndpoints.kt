package com.github.d_costa.sessionkotlin.api

import com.github.d_costa.sessionkotlin.api.exception.SKLinearException
import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.message.SKDummy
import com.github.d_costa.sessionkotlin.backend.message.SKMessage
import com.github.d_costa.sessionkotlin.backend.message.SKPayload
import java.util.*

/**
 * Linear endpoint. Throws [SKLinearException] when [SKLinearEndpoint.use] is called twice.
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
public abstract class SKOutputEndpoint(private val e: SKMPEndpoint) : SKLinearEndpoint() {

    /**
     * Sends a message with payload of type [T] to the target [role].
     *
     */
    protected suspend fun <T> send(role: SKGenRole, payload: T, branch: String?) {
        use()
        if (payload is Unit) {
            e.send(role, SKDummy(branch))
        } else {
            e.send(role, SKPayload(payload, branch))
        }
    }
}

/**
 * Abstract linear endpoint that corresponds to an input.
 */
public abstract class SKInputEndpoint(private val e: SKMPEndpoint) : SKLinearEndpoint() {

    protected open suspend fun <T : Any> receive(role: SKGenRole): SKMessage {
        use()
        return e.receive(role)
    }

    /**
     * Receive a message with payload of type [T] from [role]
     * and assign its payload to [buf]'s value.
     */
    @Suppress("unchecked_cast")
    protected suspend fun <T : Any> receivePayload(role: SKGenRole, buf: SKBuffer<T>) {
        val msg = receive<T>(role)
        if (msg is SKPayload<*>) {
            buf.value = (msg as SKPayload<T>).payload
        }
    }
}

public abstract class SKCaseEndpoint(e: SKMPEndpoint, private val msg: SKMessage) : SKInputEndpoint(e) {
    override suspend fun <@Suppress("unused") T : Any> receive(role: SKGenRole): SKMessage {
        use()
        return msg
    }
}

//
// public abstract class SKSendEndpoint(e: SKMPEndpoint): SKOutputEndpoint(e)
// public abstract class SKReceiveEndpoint(e: SKMPEndpoint): SKInputEndpoint(e) {
//
//    /**
//     * Receive a message with payload of type [T] from [role]
//     * and assign its payload to [buf]'s value.
//     */
//    @Suppress("unchecked_cast")
//    protected suspend fun <T : Any> receivePayload(role: SKGenRole, buf: SKBuffer<T>) {
//        val msg = receive<T>(role)
//        if (msg is SKPayload<*>) {
//            buf.value = (msg as SKPayload<T>).payload
//        }
//    }
//
//    /**
//     * Receive a message from [role] and ignore its content.
//     */
//    protected suspend fun receiveDummy(role: SKGenRole) {
//        receive<Any>(role)
//    }
// }
