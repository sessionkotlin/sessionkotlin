package com.github.d_costa.sessionkotlin.backend.endpoint

import io.ktor.network.sockets.*
import io.ktor.util.network.*

public class SKServerSocket(internal val ss: ServerSocket) {
    public val port: Int
        get() = ss.localAddress.toJavaAddress().port

    public val hostname: String
        get() = ss.localAddress.toJavaAddress().hostname
}
