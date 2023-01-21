package com.github.sessionkotlin.parser.util

/**
 * Gets the value corresponding to the [key]. If the key is not present, throw [throwable].
 *
 * ```
 * val myMap: Map<String, Int> = mapOf("a" to 1, "b" to 2)
 * val v: Int = myMap.getOrThrow("a") { MyCustomException() }
 * ```
 */
internal fun <K, V> Map<K, V>.getOrThrow(key: K, throwable: () -> Throwable): V {
    try {
        return getValue(key)
    } catch (e: NoSuchElementException) {
        throw throwable()
    }
}
