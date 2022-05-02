package org.david.sessionkotlin.api

import org.david.sessionkotlin.api.exception.SKLinearException
import org.david.sessionkotlin.backend.SKBranch
import org.david.sessionkotlin.backend.SKBuffer
import org.david.sessionkotlin.backend.SKMPEndpoint
import org.david.sessionkotlin.backend.SKPayload

/**
 * Linear endpoint. Throws [SKLinearException] when [SKEndpoint.use] is called twice.
 */
public open class SKEndpoint {
    private var used = false
    internal fun use() {
        if (used)
            throw SKLinearException()
        else
            used = true
    }
}

public abstract class SKOutputEndpoint(private val e: SKMPEndpoint) : SKEndpoint() {

    /**
     * Sends a message with payload of type [T] to the target [role].
     *
     * If [branch] is not null, sends a message with it,
     * before sending the message with the [payload].
     */
    protected suspend fun <T> send(role: SKGenRole, payload: T, branch: String? = null) {
        use()
        if (branch != null) {

            e.send(role, SKBranch(branch))
        }
        e.send(role, SKPayload(payload))
    }
}

@Suppress("unchecked_cast")
public abstract class SKInputEndpoint(private val e: SKMPEndpoint) : SKEndpoint() {

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

// public abstract class SKInternalEndpoint(private val e: SKMPEndpoint) : SKEndpoint() {
//    public suspend fun sendBranch(role: SKGenRole, label: String) {
//        use()
//        e.send(role, SKBranch(label))
//    }
// }

public abstract class SKExternalEndpoint(private val e: SKMPEndpoint) : SKEndpoint() {

    /**
     * Receives a branch message from [role].
     */
    protected suspend fun receiveBranch(role: SKGenRole): String {
        use()
        val msg = e.receive(role)
        return (msg as SKBranch).label
    }
}
