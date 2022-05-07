package org.david.sessionkotlin.parser.util

internal fun <V> Map<String, V>.getOrThrow(name: String, throwable: () -> Throwable): V {
    try {
        return getValue(name)
    } catch (e: NoSuchElementException) {
        throw throwable()
    }
}
