package com.github.d_costa.sessionkotlin.backend.message

import java.io.Serializable
import java.util.*

public interface SKMessage : Serializable

public data class SKPayload<T>(
    val payload: T,
    val branch: String? = null
) : SKMessage

public data class SKDummy(
    val branch: String? = null
) : SKMessage
