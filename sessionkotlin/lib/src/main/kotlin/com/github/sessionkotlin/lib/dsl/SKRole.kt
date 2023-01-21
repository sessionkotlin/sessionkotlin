package com.github.sessionkotlin.lib.dsl

import com.github.sessionkotlin.lib.dsl.exception.RoleNameWhitespaceException
import com.github.sessionkotlin.lib.util.hasWhitespace

/**
 * The SessionKotlin's role
 *
 * @param name the name of the role.
 */
public class SKRole(private val name: String) {

    init {
        if (name.hasWhitespace()) {
            throw RoleNameWhitespaceException(name)
        }
    }

    override fun toString(): String {
        return name
    }
}
