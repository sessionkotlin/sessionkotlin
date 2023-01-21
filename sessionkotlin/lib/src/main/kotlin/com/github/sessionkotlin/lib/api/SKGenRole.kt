package com.github.sessionkotlin.lib.api

/**
 * Abstract class for generated roles.
 */
public abstract class SKGenRole {
    override fun toString(): String {
        return this::class.simpleName ?: super.toString()
    }
}
