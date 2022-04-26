package org.david.sessionkotlin.util

import org.david.sessionkotlin.dsl.SKRole

internal fun printlnIndent(indent: Int, message: Any?) {
    println(" ".repeat(indent) + message)
}

internal fun Map<SKRole, SKRole>.getOrKey(key: SKRole): SKRole = this.getOrDefault(key, key)

internal fun String.asClassname() =
    this.replace(" ", "")
        .replaceFirstChar(Char::titlecase)
