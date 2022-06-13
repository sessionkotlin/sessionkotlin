package com.github.d_costa.sessionkotlin.backend.endpoint

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers

public class SKServer {

    public companion object {
        /**
         * Service that manages NIO selectors and selection threads.
         */
        private val selectorManager = ActorSelectorManager(Dispatchers.IO)

        /**
         * Create a server socket and bind it to [port].
         */
        public fun bind(port: Int): SKServerSocket {
            return SKServerSocket(
                aSocket(selectorManager)
                    .tcp()
                    .bind(InetSocketAddress("localhost", port))
            )
        }
    }
}
