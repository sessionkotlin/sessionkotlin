package com.github.d_costa.sessionkotlin.util

import com.github.d_costa.sessionkotlin.api.exception.RefinementException
import com.github.d_costa.sessionkotlin.dsl.SKRole

internal fun printlnIndent(indent: Int, message: Any?) {
    println(" ".repeat(indent) + message)
}

internal fun Map<SKRole, SKRole>.getOrKey(key: SKRole): SKRole = this.getOrDefault(key, key)

internal fun String.asClassname() =
    this.replace("\\s".toRegex(), "")
        .capitalized()

internal fun String.capitalized() =
    replaceFirstChar(Char::titlecase)

/**
 * Throws [RefinementException] if evaluation is false.
 */
public fun assertRefinement(refinement: String, evaluation: Boolean) {
    if (!evaluation)
        throw RefinementException(refinement)
}
