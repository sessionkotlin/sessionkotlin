package org.david.sessionkotlin.api

import org.david.sessionkotlin.api.exception.SKLinearException
import org.david.sessionkotlin.backend.SKBranch
import org.david.sessionkotlin.backend.SKBuffer
import org.david.sessionkotlin.backend.SKMPEndpoint
import org.david.sessionkotlin.backend.SKPayload

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
    public suspend fun <T> send(role: SKGenRole, payload: T, label: String? = null) {
        use()
        if (label != null) {

            e.send(role, SKBranch(label))
        }
        e.send(role, SKPayload(payload))
    }
}

@Suppress("unchecked_cast")
public abstract class SKInputEndpoint(private val e: SKMPEndpoint) : SKEndpoint() {
    public suspend fun <T : Any> receive(role: SKGenRole, buf: SKBuffer<T>) {
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
    public suspend fun receiveBranch(role: SKGenRole): String {
        use()
        val msg = e.receive(role)
        return (msg as SKBranch).label
    }
}
