package com.github.d_costa.sessionkotlin.dsl

/**
 * The SessionKotlin's role
 *
 * @param name the name of the role.
 */
public class SKRole(private val name: String) {

    override fun toString(): String {
        return name
    }
}
