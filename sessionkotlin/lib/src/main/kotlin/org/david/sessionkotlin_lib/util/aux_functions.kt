package org.david.sessionkotlin_lib.util

import org.david.sessionkotlin_lib.dsl.Role

internal fun printlnIndent(indent: Int, message: Any?) {
    println(" ".repeat(indent) + message)
}

internal fun Map<Role, Role>.getOrKey(key: Role): Role = this.getOrDefault(key, key)

internal fun String.asValidName() =
    this.replace(" ", "")
