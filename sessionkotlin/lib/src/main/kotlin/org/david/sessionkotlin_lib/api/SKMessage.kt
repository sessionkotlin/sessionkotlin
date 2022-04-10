package org.david.sessionkotlin_lib.api

public interface SKMessage : java.io.Serializable

internal data class SKBranch(val label: String) : SKMessage
internal data class SKPayload<T>(val payload: T) : SKMessage
