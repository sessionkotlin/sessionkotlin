package org.david.sessionkotlin_lib.dsl.util

import org.david.sessionkotlin_lib.dsl.Role

internal fun printlnIndent(indent: Int, message: Any?) {
    println(" ".repeat(indent) + message)
}

fun Map<Role, Role>.getOrKey(key: Role) = this.getOrDefault(key, key)
