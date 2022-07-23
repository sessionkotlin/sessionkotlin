package impl

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers

fun createSocket(selector: SelectorManager) = aSocket(selector).tcp().bind("localhost", 0)
