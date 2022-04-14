package org.david.sessionkotlin_lib.api

import org.david.sessionkotlin_lib.backend.SKBranch
import org.david.sessionkotlin_lib.backend.SKBuffer
import org.david.sessionkotlin_lib.backend.SKMPEndpoint
import org.david.sessionkotlin_lib.backend.SKPayload

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
    public suspend fun <T> send(role: SKGenRole, payload: T) {
        use()
        e.send(role, SKPayload(payload))
    }
}

public abstract class SKInputEndpoint(private val e: SKMPEndpoint) : SKEndpoint() {
    public suspend fun <T> receive(role: SKGenRole, buf: SKBuffer<T>) {
        use()
        val msg = e.receive(role)
        buf.value = (msg as SKPayload<T>).payload
    }
}

public abstract class SKInternalEndpoint(private val e: SKMPEndpoint) : SKEndpoint() {
    public suspend fun sendBranch(role: SKGenRole, label: String) {
        use()
        e.send(role, SKBranch(label))
    }
}

public abstract class SKExternalEndpoint(private val e: SKMPEndpoint) : SKEndpoint() {
    public suspend fun receiveBranch(role: SKGenRole): String {
        use()
        val msg = e.receive(role)
        return (msg as SKBranch).label
    }
}
