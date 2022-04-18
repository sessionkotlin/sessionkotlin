package org.david.sessionkotlin_lib.backend.channel

import kotlinx.coroutines.channels.Channel
import org.david.sessionkotlin_lib.api.SKGenRole
import org.david.sessionkotlin_lib.backend.SKMessage
import org.david.sessionkotlin_lib.backend.exception.BinaryEndpointsException

/**
 * Declares a bidirectional channel between two roles.
 */
public class SKChannel(private val role1: SKGenRole, private val role2: SKGenRole) {

    private val chanA = Channel<SKMessage>(Channel.UNLIMITED)
    private val chanB = Channel<SKMessage>(Channel.UNLIMITED)

    private val mapping = mapOf(
        role1 to SKDoubleChannel(chanA, chanB),
        role2 to SKDoubleChannel(chanB, chanA)
    )

    internal fun getEndpoints(role: SKGenRole): SKDoubleChannel =
        try {
            mapping.getValue(role)
        } catch (e: NoSuchElementException) {
            throw BinaryEndpointsException(role, role1, role2)
        }
}
