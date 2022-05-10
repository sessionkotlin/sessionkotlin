package com.github.d_costa.sessionkotlin.api

/**
 * Base class for generated roles.
 */
public abstract class SKGenRole {
    override fun toString(): String {
        return this::class.simpleName ?: super.toString()
    }
}
