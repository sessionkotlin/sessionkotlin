package org.david.sessionkotlin.backend

import java.io.Serializable

public sealed interface SKMessage : Serializable

public data class SKBranch(val label: String) : SKMessage
public data class SKPayload<T>(val payload: T) : SKMessage
