package com.github.d_costa.sessionkotlin.dsl

import com.github.d_costa.sessionkotlin.dsl.exception.RoleNameWhitespaceException
import com.github.d_costa.sessionkotlin.util.hasWhitespace

/**
 * The SessionKotlin's role
 *
 * @param name the name of the role.
 */
public class SKRole(private val name: String) {

    init {
        if (hasWhitespace(name)) {
            throw RoleNameWhitespaceException(name)
        }
    }

    override fun toString(): String {
        return name
    }
}
