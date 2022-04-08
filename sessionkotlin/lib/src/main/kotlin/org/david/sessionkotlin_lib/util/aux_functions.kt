package org.david.sessionkotlin_lib.util

import org.david.sessionkotlin_lib.dsl.SKRole

internal fun printlnIndent(indent: Int, message: Any?) {
    println(" ".repeat(indent) + message)
}

internal fun Map<SKRole, SKRole>.getOrKey(key: SKRole): SKRole = this.getOrDefault(key, key)

internal fun String.remWhitespace() =
    this.replace(" ", "")

internal fun String.capitalized() =
    replaceFirstChar(Char::titlecase)
