package org.david.sessionkotlin.api

public abstract class SKGenRole {
    override fun toString(): String {
        return this::class.simpleName ?: super.toString()
    }
}
