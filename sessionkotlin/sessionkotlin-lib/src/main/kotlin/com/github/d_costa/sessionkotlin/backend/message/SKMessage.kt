package com.github.d_costa.sessionkotlin.backend.message

import java.io.Serializable
import java.util.*

public open class SKMessage(
    public val payload: Any,
    public val branch: String? = null
) : Serializable {
    override fun toString(): String {
        return "SKMessage(payload: $payload, branch: $branch)"
    }
    override fun equals(other: Any?): Boolean {
        if (other !is SKMessage)
            return false

        return payload == other.payload && branch == other.branch
    }

    override fun hashCode(): Int {
        var result = payload.hashCode()
        result = 31 * result + (branch?.hashCode() ?: 0)
        return result
    }
}

public class SKDummyMessage(branch: String?) : SKMessage("", branch)
