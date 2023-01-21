package com.github.sessionkotlin.lib.backend.endpoint

import io.ktor.network.sockets.*
import io.ktor.util.network.*
import java.io.Closeable

public class SKServerSocket internal constructor(internal val ss: ServerSocket) : Closeable {

    public val port: Int
        get() = ss.localAddress.toJavaAddress().port

    public val hostname: String
        get() = ss.localAddress.toJavaAddress().hostname

    public override fun close() {
        ss.close()
    }
}
