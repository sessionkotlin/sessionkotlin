package org.david.sessionkotlin_lib.backend.channel

import kotlinx.coroutines.channels.Channel
import org.david.sessionkotlin_lib.api.SKGenRole
import org.david.sessionkotlin_lib.backend.SKMessage
import org.david.sessionkotlin_lib.backend.exception.BinaryEndpointsException

/**
 * Declares a bidirectional channel between two roles.
 *
 * If a role is not specified, its definition is deferred until [getEndpoints] is invoked.
 *
 */
public class SKChannel(private var role1: SKGenRole? = null, private var role2: SKGenRole? = null) {

    private val chanA = Channel<SKMessage>(Channel.UNLIMITED)
    private val chanB = Channel<SKMessage>(Channel.UNLIMITED)

    private val doubleChan1 = SKDoubleChannel(chanA, chanB)
    private val doubleChan2 = SKDoubleChannel(chanB, chanA)

    internal fun getEndpoints(role: SKGenRole): SKDoubleChannel {
        if (role1 == null) {
            role1 = role
        } else if (role2 == null && role1 != role) {
            role2 = role
        }

        return when (role) {
            role1 -> {
                doubleChan1
            }
            role2 -> {
                doubleChan2
            }
            else -> {
                throw BinaryEndpointsException(role)
            }
        }
    }
}
