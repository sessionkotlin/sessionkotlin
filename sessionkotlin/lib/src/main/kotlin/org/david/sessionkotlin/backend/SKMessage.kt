package org.david.sessionkotlin.backend

import java.io.Serializable

/**
 * The basic message that will be transferred between endpoints.
 */
public sealed interface SKMessage : Serializable

/**
 * A message that contains information about a branch [label].
 */
public data class SKBranch(val label: String) : SKMessage

/**
 * A message that contains a [payload] of type [T]
 */
public data class SKPayload<T>(val payload: T) : SKMessage
