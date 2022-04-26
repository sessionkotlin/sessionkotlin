package org.david.sessionkotlin.backend

import java.io.Serializable

internal sealed interface SKMessage : Serializable

internal data class SKBranch(val label: String) : SKMessage
internal data class SKPayload<T>(val payload: T) : SKMessage
