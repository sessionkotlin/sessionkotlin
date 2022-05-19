package com.github.d_costa.sessionkotlin.api

import com.github.d_costa.sessionkotlin.api.exception.SKLinearException
import com.github.d_costa.sessionkotlin.backend.SKBuffer
import com.github.d_costa.sessionkotlin.backend.endpoint.SKMPEndpoint
import com.github.d_costa.sessionkotlin.backend.message.SKBranch
import com.github.d_costa.sessionkotlin.backend.message.SKPayload

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
     * If [branch] is not null, an [SKBranch] message is sent
     * before the [payload] message.
     */
    protected suspend fun <T> send(role: SKGenRole, payload: T, branch: String? = null) {
        use()
        if (branch != null) {

            e.send(role, SKBranch(branch))
        }
        e.send(role, SKPayload(payload))
    }
}

/**
 * Abstract linear endpoint that corresponds to an input.
 */
@Suppress("unchecked_cast")
public abstract class SKInputEndpoint(private val e: SKMPEndpoint) : SKLinearEndpoint() {

    /**
     * Receives a message with payload of type [T] from [role]
     * and assigns its payload to [buf]'s value.
     */
    protected suspend fun <T : Any> receive(role: SKGenRole, buf: SKBuffer<T>) {
        use()
        val msg = e.receive(role)
        buf.value = (msg as SKPayload<T>).payload
    }
}

/**
 * Abstract linear endpoint that corresponds to an external choice.
 */
public abstract class SKExternalEndpoint(private val e: SKMPEndpoint) : SKLinearEndpoint() {

    /**
     * Receives a branch message from [role].
     *
     * @return the branch label
     */
    protected suspend fun receiveBranch(role: SKGenRole): String {
        use()
        val msg = e.receive(role)
        return (msg as SKBranch).label
    }
}
