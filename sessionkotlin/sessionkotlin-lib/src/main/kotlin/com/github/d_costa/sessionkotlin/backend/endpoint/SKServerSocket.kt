package com.github.d_costa.sessionkotlin.backend.endpoint

import io.ktor.network.sockets.*
import io.ktor.util.network.*

public class SKServerSocket(internal val ss: ServerSocket) {
    public fun port(): Int = ss.localAddress.toJavaAddress().port
    public fun hostname(): String = ss.localAddress.toJavaAddress().hostname
}
