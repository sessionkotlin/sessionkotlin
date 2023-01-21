package com.github.sessionkotlin.lib.backend.message

import java.io.Serializable

public open class SKMessage(
    public val label: String,
    public val payload: Any
) : Serializable {

    public companion object {
        public const val DEFAULT_LABEL: String = "EMPTY_LABEL"
        public const val DUMMY_MSG_PAYLOAD: String = "DUMMY_MSG_PAYLOAD"
    }

    override fun toString(): String {
        return "SKMessage[$label]($payload)"
    }
    override fun equals(other: Any?): Boolean {
        if (other !is SKMessage)
            return false

        return payload == other.payload && label == other.label
    }

    override fun hashCode(): Int {
        var result = payload.hashCode()
        result = 31 * result + label.hashCode()
        return result
    }
}

public class SKDummyMessage(label: String) : SKMessage(label, DUMMY_MSG_PAYLOAD)
