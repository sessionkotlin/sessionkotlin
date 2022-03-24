package org.david.sessionkotlin_lib.dsl

internal fun printlnIndent(indent: Int, message: Any?) {
    println(" ".repeat(indent) + message)
}
